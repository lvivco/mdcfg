package org.mdcfg.processor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Alias {
    private String targetDimension;
    private String from;
    private String to;
}
