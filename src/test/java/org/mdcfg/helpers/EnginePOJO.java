package org.mdcfg.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EnginePOJO {
    @Data
    public class BlockPOJO {
        private String type;
        @JsonProperty("cylinder-count")
        private int cylinderCount;
    }

    private String type;
    private String drive;
    private BlockPOJO block;
}
