package nny.build.data.builder.config.sinkConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Setter
@Getter
public class KafkaSinkConfig extends SinkConfig implements Serializable {
    private static final long serialVersionUID = -3611374762308139278L;


    private String bootStrapServers;
    private String topics;

}
