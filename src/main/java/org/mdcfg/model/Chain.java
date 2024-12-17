/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mdcfg.provider.MdcContext;

import java.util.List;
import java.util.regex.Pattern;

/**
 *  Represents one config chain.
 *  <p> For example property:
 *  <pre>
 *    horsepower:
 *      any@: 400
 *      model@bmw:
 *        drive@4WD: 500
 *  </pre>
 *  contains two chains:
 *  <ul>
 *  <li>{@code model@any.drive@any}</li>
 *  <li>{@code model@bmw.drive@4WD}</li>
 *  </ul>
 *  Those chains will be represented in RegExps:
 *  <ul>
 *  <li>{@code model@.*\.drive@.*$}</li>
 *  <li>{@code model@bmw\.drive@4WD$}</li>
 *  </ul>
 */
@AllArgsConstructor
public class Chain {
    private Pattern plusPattern;
    private Pattern minusPattern;
    @Getter private String value;
    private List<Range> ranges;

    /** Check whether chain matches context */
    public boolean match(MdcContext context, String compare) {
        boolean minusMatch = minusPattern != null && minusPattern.matcher(compare).matches();
        boolean match = !minusMatch && plusPattern.matcher(compare).matches();
        return match && !ranges.isEmpty()
                ? ranges.stream().anyMatch(range -> range.matches(context))
                : match;
    }
}
