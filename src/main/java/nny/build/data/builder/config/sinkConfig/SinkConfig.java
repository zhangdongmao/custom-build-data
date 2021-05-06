package nny.build.data.builder.config.sinkConfig;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nny.build.data.builder.model.rule.ValueRule;

import java.io.Serializable;

@Slf4j
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true, property = "type")
@JsonSubTypes({
        // kafka
        @JsonSubTypes.Type(value = KafkaSinkConfig.class, name = "kafka"),
        // csv
        @JsonSubTypes.Type(value = CsvSinkConfig.class, name = "csv"),
})
public class SinkConfig extends AbstractSinkConfig implements Serializable {
    private static final long serialVersionUID = -3758710688446604562L;

}
