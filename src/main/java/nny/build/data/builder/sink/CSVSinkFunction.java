package nny.build.data.builder.sink;

import lombok.extern.slf4j.Slf4j;
import nny.build.data.builder.config.BuilderConfig;
import nny.build.data.builder.config.BuilderConfigLoader;
import nny.build.data.builder.config.sinkConfig.CsvSinkConfig;
import nny.build.data.builder.config.sinkConfig.SinkConfig;
import nny.build.data.builder.exception.BuilderException;
import nny.build.data.builder.model.build.BuildData;
import nny.build.data.builder.model.build.BuildExpression;
import nny.build.data.builder.model.build.BuildSqlData;
import nny.build.data.builder.model.table.TableColumn;
import nny.build.data.builder.model.table.TableInfo;
import nny.build.data.builder.model.table.TableRelation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CSVSinkFunction implements ISinkFunction {
    @Override
    public void initialize() {

    }

    @Override
    public void invoke(List<BuildData> buildDataList) {
        log.info("开始写入CSV文件");
        BuilderConfig builderConfig = BuilderConfigLoader.getBuilderConfig();
        CsvSinkConfig sinkConfig = (CsvSinkConfig)builderConfig.getSinkConfig();
        String csvSinkFilePath = sinkConfig.getCsvSinkFilePath();
        ConcurrentHashMap<String,List<String>> CSVMap = new ConcurrentHashMap<>();

        if (StringUtils.isNotEmpty(csvSinkFilePath)){
            for (BuildData buildData : buildDataList){
                ConcurrentHashMap<String, List<String>> conversion = conversion(buildData.getTableInfos());
                CSVMap = mergeCSVMap(CSVMap,conversion);
            }

            if (!CSVMap.isEmpty()){
                writeToFile(csvSinkFilePath,CSVMap);
                CSVMap.clear();
            }
        }else {
            log.error("未配置CSV sink文件路径,对应参数为：{}","CSVSinkFilePath");
        }

    }

    public ConcurrentHashMap<String, List<String>> mergeCSVMap(ConcurrentHashMap<String, List<String>> CSVMap1,ConcurrentHashMap<String, List<String>> CSVMap2){
        if (CSVMap1.isEmpty()){
            return CSVMap2;
        }else if (CSVMap2.isEmpty()){
            return CSVMap1;
        }
        for (Map.Entry<String, List<String>> entry :CSVMap1.entrySet()){
            if (CSVMap2.containsKey(entry.getKey())){
                CSVMap2.get(entry.getKey()).addAll(entry.getValue());
            }else {
                CSVMap2.put(entry.getKey(),entry.getValue());
            }
        }
        return CSVMap2;
    }

    public void writeToFile(String csvSinkFilePath, ConcurrentHashMap<String,List<String>> CSVMap){
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            for (Map.Entry<String,List<String>> entry : CSVMap.entrySet()){
                fileWriter = new FileWriter(csvSinkFilePath+File.separator+entry.getKey(),true);
                bufferedWriter = new BufferedWriter(fileWriter);

                for (int i=0;i<entry.getValue().size();i++){
                    bufferedWriter.write(entry.getValue().get(i));
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
            }


        } catch (FileNotFoundException e) {
            log.error("CSV sink文件不存在：{}",csvSinkFilePath);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 填充sqlDataMap
     */
    public ConcurrentHashMap<String,List<String>> conversion(List<TableInfo> tableInfos) {

        if (tableInfos == null) {
            throw new BuilderException("tableInfos不能为空");
        }

        return conversionTableInfos(tableInfos);
    }

    private ConcurrentHashMap<String,List<String>> conversionTableInfos(List<TableInfo> tableInfos) {
        ConcurrentHashMap<String,List<String>> CSVMap = new ConcurrentHashMap<>();
            for (TableInfo tableInfo : tableInfos) {

                BuildExpression tableCondition = tableInfo.getTableCondition();

                if (tableCondition != null && !tableCondition.getExpressionBoolResult()) {
                    continue;
                }

                if (CSVMap.containsKey(tableInfo.getTableName())){
                    CSVMap.get(tableInfo.getTableName()).add(conversionCSV(tableInfo));
                }else {
                    List<String> CSVList = Collections.synchronizedList(new ArrayList<>());
                    CSVList.add(conversionCSV(tableInfo));
                    CSVMap.put(tableInfo.getTableName(),CSVList);
                }

                List<TableRelation> relations = tableInfo.getRelations();

                if (CollectionUtils.isNotEmpty(relations)) {
                    CSVMap = mergeCSVMap(CSVMap,conversionTableRelation(relations));
                }
            }
            return CSVMap;
    }
    private ConcurrentHashMap<String,List<String>> conversionTableRelation(List<TableRelation> relations) {
        ConcurrentHashMap<String,List<String>> CSVMap = new ConcurrentHashMap<>();
        for (TableRelation relation : relations) {

            if (CSVMap.containsKey(relation.getRelationTable().getTableName())){
                CSVMap.get(relation.getRelationTable().getTableName()).addAll(conversionCSV(relation));
            }else {
                CSVMap.put(relation.getRelationTable().getTableName(),conversionCSV(relation));
            }

            if (CollectionUtils.isNotEmpty(relation.getRelationTable().getRelations())) {
                conversionTableRelation(relation.getRelationTable().getRelations());
            }
        }
        return CSVMap;
    }

    private String conversionCSV(TableInfo tableInfo) {

        StringBuffer sb = new StringBuffer();
        List<TableColumn> columns = tableInfo.getColumns();
        for (int i=0;i<columns.size();i++) {
            if (i==(columns.size()-1)){
                sb.append(columns.get(i).getColumnValue());
            }else{
                sb.append(columns.get(i).getColumnValue()).append(",");
            }

        }

        return sb.toString();
    }

    private List<String> conversionCSV(TableRelation tableRelation) {
        List<String> CSVList = Collections.synchronizedList(new ArrayList<>());
        List<List<TableColumn>> relationColumns = tableRelation.getRelationColumns();

        for (List<TableColumn> relationColumnList:  relationColumns){
            StringBuffer sb = new StringBuffer();

            for (int i=0;i<relationColumnList.size();i++) {
                if (i==(relationColumnList.size()-1)){
                    sb.append(relationColumnList.get(i).getColumnValue());
                }else{
                    sb.append(relationColumnList.get(i).getColumnValue()).append(",");
                }
            }
            CSVList.add(sb.toString());
        }
        return CSVList;
    }
}
