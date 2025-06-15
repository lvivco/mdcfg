/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mdcfg.provider.MdcContext;
import java.util.*;


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
    private final Map<String, Selector> selectors;
    @Getter private String value;
    private final List<Range> ranges;

    /** Check whether chain matches context */
    public boolean match(MdcContext context, boolean isCaseSensitive) {
        boolean hasNegative = false;
        boolean negativeMatch = true;
        for (var entry : selectors.entrySet()) {
            Selector selector = entry.getValue();
            Object ctxVal = context.get(entry.getKey());
            if (selector.isNegative()) {
                hasNegative = true;
                if (!selector.matches(ctxVal, isCaseSensitive)) {
                    negativeMatch = false;
                }
            } else {
                if (!selector.matches(ctxVal, isCaseSensitive)) {
                    return false;
                }
            }
        }

        if (hasNegative && negativeMatch) {
            return false;
        }

        if (!ranges.isEmpty()) {
            return ranges.stream().anyMatch(range -> range.matches(context));
        }
        return true;
    }


}
