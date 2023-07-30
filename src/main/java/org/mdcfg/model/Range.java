package org.mdcfg.model;

import org.mdcfg.provider.MdcContext;
import org.mdcfg.utils.ProviderUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

public class Range {

    private final Dimension dimension;

    private double min = Double.MIN_VALUE;
    private boolean minInclusive = true;
    private double max= Double.MAX_VALUE;
    private boolean maxInclusive = true;

    public Range(Dimension dimension, String min, String max) {
        this.dimension = dimension;

        if(StringUtils.isNotBlank(min)){
            if(min.startsWith("!")){
                min = min.substring(1);
                minInclusive = false;
            }
            this.min = Double.parseDouble(min);
        }

        if(StringUtils.isNotBlank(max)){
            if(max.startsWith("!")){
                max = max.substring(1);
                maxInclusive = false;
            }
            this.max = Double.parseDouble(max);
        }
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;
        Range range = (Range) o;
        return Double.compare(range.min, min) == 0 && minInclusive == range.minInclusive && Double.compare(range.max, max) == 0 && maxInclusive == range.maxInclusive && dimension.equals(range.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, min, minInclusive, max, maxInclusive);
    }



    @Override
    public String toString() {
        return "Range{" +
                "dimension=" + dimension +
                ", min=" + min +
                ", minInclusive=" + minInclusive +
                ", max=" + max +
                ", maxInclusive=" + maxInclusive +
                '}';
    }
}
