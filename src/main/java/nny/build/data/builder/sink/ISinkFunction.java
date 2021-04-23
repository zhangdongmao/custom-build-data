package nny.build.data.builder.sink;

import nny.build.data.builder.model.InState;
import nny.build.data.builder.model.build.BuildData;
import nny.build.data.builder.model.table.TableInfo;

import java.util.List;

public interface ISinkFunction {
    /**
     * 初始化
     *
     */
    void initialize();

    /**
     * 输出数据
     *
     * @param buildDataList 生成的数据
     */
    void invoke(List<BuildData> buildDataList);
}
