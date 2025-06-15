/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.mdcfg.provider.MdcContext;
import org.mdcfg.utils.ProviderUtils;

import java.util.List;

/**
 * Helper for number comparing.
 */
@EqualsAndHashCode
@ToString
public class Range {
    private final Dimension dimension;
    private double min = -Double.MAX_VALUE;
    private final boolean minInclusive;
    private double max= Double.MAX_VALUE;
    private final boolean maxInclusive;

    public Range(Dimension dimension, boolean minInclusive, String min, boolean maxInclusive, String max) {
        this.dimension = dimension;
        this.minInclusive = minInclusive;
        if (StringUtils.isNotBlank(min)) {
            this.min = Double.parseDouble(min);
        }
        if (StringUtils.isNotBlank(max)) {
            this.max = Double.parseDouble(max);
        }
        this.maxInclusive = maxInclusive;
    }

    /** Check whether context value fits number range. */
    public boolean matches(MdcContext context) {
        if(!context.containsKey(dimension.getName())){
            return false;
        }
        Object object = context.get(dimension.getName());
        if(object != null) {
            List<?> list = ProviderUtils.toList(object);
            if(list != null){
                return list.stream().anyMatch(this::matches);
            } else {
                return matches(object);
            }
        }
        return false;
    }

    private boolean matches(Object object) {
        double value = Double.parseDouble(object.toString());
        boolean minMatch = minInclusive ? value >= min : value > min;
        boolean maxMatch = maxInclusive ? value <= max : value < max;
        return minMatch && maxMatch;
    }

}
