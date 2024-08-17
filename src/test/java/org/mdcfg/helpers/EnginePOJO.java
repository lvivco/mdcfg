/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.helpers;

import lombok.Data;

@Data
public class EnginePOJO {
    private String type;
    private String drive;
    private BlockPOJO block;
}
