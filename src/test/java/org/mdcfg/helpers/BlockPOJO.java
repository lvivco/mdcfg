/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BlockPOJO {
    private String type;
    @JsonProperty("cylinder-count")
    private int cylinderCount;
}