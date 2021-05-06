package nny.build.data.builder.config.sinkConfig;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nny.build.data.builder.model.rule.*;

import java.io.Serializable;

@Slf4j
@Getter
@Setter
public abstract class AbstractSinkConfig implements Serializable {
    private static final long serialVersionUID = 4566979213898556123L;

    /**
     * sink类型
     */
    private String type;

}
