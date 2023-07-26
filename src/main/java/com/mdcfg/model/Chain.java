package com.mdcfg.model;

import com.mdcfg.provider.MdcContext;

import java.util.List;
import java.util.regex.Pattern;

public class Chain {

    private Pattern pattern;
    private String value;
    private List<Range> ranges;

    public Chain(String chain, String value, List<Range> ranges) {
        this.pattern = Pattern.compile(chain);
        this.value = value;
        this.ranges = ranges;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean match(MdcContext context, String compare) {
        boolean match = pattern.matcher(compare).matches();
        if(match && !ranges.isEmpty()){
            for (Range range : ranges) {
                if(!range.matches(context)){
                    return false;
                }
            }
        }
        return match;
    }
}
