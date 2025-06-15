/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mdcfg.provider.MdcContext;
import org.mdcfg.utils.ProviderUtils;

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
    private final Map<String, SelectorData> plusSelectors;
    private final Map<String, SelectorData> minusSelectors;
    @Getter private String value;
    private final List<Range> ranges;

    /** Check whether chain matches context */
    public boolean match(MdcContext context, boolean isCaseSensitive) {
        if (!minusSelectors.isEmpty()) {
            boolean minusMatch = true;
            for (var entry : minusSelectors.entrySet()) {
                Object ctxVal = context.get(entry.getKey());
                if (!entry.getValue().matches(ctxVal, isCaseSensitive)) {
                    minusMatch = false;
                    break;
                }
            }
            if (minusMatch) {
                return false;
            }
        }

        for (var entry : plusSelectors.entrySet()) {
            Object ctxVal = context.get(entry.getKey());
            if (!entry.getValue().matches(ctxVal, isCaseSensitive)) {
                return false;
            }
        }

        if (!ranges.isEmpty()) {
            return ranges.stream().anyMatch(range -> range.matches(context));
        }
        return true;
    }

    /** Helper that stores selector information for one dimension */
    @AllArgsConstructor
    public static class SelectorData {
        private final boolean list;
        private final List<String> values;
        private final boolean any;

        boolean matches(Object value, boolean isCaseSensitive) {
            if (any) {
                return true;
            }
            if (value == null) {
                return false;
            }
            List<?> listVal = ProviderUtils.toList(value);
            if (list && listVal != null) {
                for (Object val : listVal) {
                    if (compare(val.toString(), isCaseSensitive)) {
                        return true;
                    }
                }
                return false;
            }
            return compare(value.toString(), isCaseSensitive);
        }

        private boolean compare(String val, boolean isCaseSensitive) {
            for (String allowed : values) {
                if (isCaseSensitive ? val.equals(allowed) : val.equalsIgnoreCase(allowed)) {
                    return true;
                }
            }
            return false;
        }
    }
}
