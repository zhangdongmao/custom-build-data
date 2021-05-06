package nny.build.data.builder.config.sinkConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Getter
@Setter
public class CsvSinkConfig extends SinkConfig implements Serializable {

    private String csvSinkFilePath;
}
