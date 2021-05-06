package nny.build.data.builder.sink;

import lombok.extern.slf4j.Slf4j;
import nny.build.data.builder.config.BuilderConfig;
import nny.build.data.builder.config.BuilderConfigLoader;
import nny.build.data.builder.config.sinkConfig.CsvSinkConfig;
import nny.build.data.builder.config.sinkConfig.KafkaSinkConfig;
import nny.build.data.builder.exception.BuilderException;
import nny.build.data.builder.model.build.BuildData;
import nny.build.data.builder.model.build.BuildExpression;
import nny.build.data.builder.model.table.TableColumn;
import nny.build.data.builder.model.table.TableInfo;
import nny.build.data.builder.model.table.TableRelation;
import nny.build.data.builder.utils.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class KafkaSinkFunction implements ISinkFunction {
    @Override
    public void initialize() {

    }

    @Override
    public void invoke(List<BuildData> buildDataList) {
        log.info("开始发送至kafka");
        BuilderConfig builderConfig = BuilderConfigLoader.getBuilderConfig();
        KafkaSinkConfig sinkConfig = (KafkaSinkConfig) builderConfig.getSinkConfig();

        ConcurrentHashMap<String, List<String>> CSVMap = new ConcurrentHashMap<>();

        if (StringUtils.isNotEmpty(sinkConfig.getTopics())) {
            for (BuildData buildData : buildDataList) {
                ConcurrentHashMap<String, List<String>> conversion = conversion(buildData.getTableInfos());
                CSVMap = CommonUtils.mergeCSVMap(CSVMap, conversion);
            }

            if (!CSVMap.isEmpty()) {
                send(sinkConfig, CSVMap);
                CSVMap.clear();
            }
        } else {
            log.error("未配置CSV sink文件路径,对应参数为：{}", "CSVSinkFilePath");
        }
    }

    public ConcurrentHashMap<String, String> parseTopics(String topics) {
        ConcurrentHashMap<String, String> topicMap = new ConcurrentHashMap<>();
        String[] topicArray = topics.replaceAll(" ", "").split(",");
        for (int i = 0; i < topicArray.length; i++) {
            String[] topic = topicArray[i].split("->");
            topicMap.put(topic[0], topic[1]);
        }
        return topicMap;
    }

    public void send(KafkaSinkConfig sinkConfig, ConcurrentHashMap<String, List<String>> CSVMap) {
        ConcurrentHashMap<String, String> topicMap = parseTopics(sinkConfig.getTopics());
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", sinkConfig.getBootStrapServers());
        properties.setProperty("acks", "all");
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        for (Map.Entry<String, List<String>> entry : CSVMap.entrySet()) {
            properties.setProperty("topic", topicMap.get(entry.getKey()));
            Producer<String, String> producer = new KafkaProducer<String, String>(properties);

            for (int i = 0; i < entry.getValue().size(); i++) {
//                producer.send(new ProducerRecord<String, String>("test_streaming", Integer.toString(i), Integer.toString(i)));

                producer.send(new ProducerRecord<String, String>(topicMap.get(entry.getKey()), Integer.toString(i), entry.getValue().get(i)));
            }
            producer.close();
        }
    }


    /**
     * 填充sqlDataMap
     */
    public ConcurrentHashMap<String, List<String>> conversion(List<TableInfo> tableInfos) {

        if (tableInfos == null) {
            throw new BuilderException("tableInfos不能为空");
        }

        return conversionTableInfos(tableInfos);
    }

    private ConcurrentHashMap<String, List<String>> conversionTableInfos(List<TableInfo> tableInfos) {
        ConcurrentHashMap<String, List<String>> CSVMap = new ConcurrentHashMap<>();
        for (TableInfo tableInfo : tableInfos) {

            BuildExpression tableCondition = tableInfo.getTableCondition();

            if (tableCondition != null && !tableCondition.getExpressionBoolResult()) {
                continue;
            }

            if (CSVMap.containsKey(tableInfo.getDbKey() + "." + tableInfo.getTableName())) {
                CSVMap.get(tableInfo.getDbKey() + "." + tableInfo.getTableName()).add(conversionCSV(tableInfo));
            } else {
                List<String> CSVList = Collections.synchronizedList(new ArrayList<>());
                CSVList.add(conversionCSV(tableInfo));
                CSVMap.put(tableInfo.getDbKey() + "." + tableInfo.getTableName(), CSVList);
            }

            List<TableRelation> relations = tableInfo.getRelations();

            if (CollectionUtils.isNotEmpty(relations)) {
                CSVMap = CommonUtils.mergeCSVMap(CSVMap, conversionTableRelation(relations));
            }
        }
        return CSVMap;
    }

    private ConcurrentHashMap<String, List<String>> conversionTableRelation(List<TableRelation> relations) {
        ConcurrentHashMap<String, List<String>> CSVMap = new ConcurrentHashMap<>();
        for (TableRelation relation : relations) {

            if (CSVMap.containsKey(relation.getRelationTable().getDbKey() + "." + relation.getRelationTable().getTableName())) {
                CSVMap.get(relation.getRelationTable().getDbKey() + "." + relation.getRelationTable().getTableName()).addAll(conversionCSV(relation));
            } else {
                CSVMap.put(relation.getRelationTable().getDbKey() + "." + relation.getRelationTable().getTableName(), conversionCSV(relation));
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
        for (int i = 0; i < columns.size(); i++) {
            if (i == (columns.size() - 1)) {
                sb.append(columns.get(i).getColumnValue());
            } else {
                sb.append(columns.get(i).getColumnValue()).append(",");
            }

        }

        return sb.toString();
    }

    private List<String> conversionCSV(TableRelation tableRelation) {
        List<String> CSVList = Collections.synchronizedList(new ArrayList<>());
        List<List<TableColumn>> relationColumns = tableRelation.getRelationColumns();

        for (List<TableColumn> relationColumnList : relationColumns) {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < relationColumnList.size(); i++) {
                if (i == (relationColumnList.size() - 1)) {
                    sb.append(relationColumnList.get(i).getColumnValue());
                } else {
                    sb.append(relationColumnList.get(i).getColumnValue()).append(",");
                }
            }
            CSVList.add(sb.toString());
        }
        return CSVList;
    }
}
