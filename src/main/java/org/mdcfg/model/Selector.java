package org.mdcfg.model;

import lombok.AllArgsConstructor;

import org.mdcfg.utils.ProviderUtils;

import java.util.List;

/**
 * Represents single selector inside a chain. It knows
 * whether it is positive or negative and can match a context value.
 */
@AllArgsConstructor
public class Selector {
    private final boolean negative;
    private final boolean list;
    private final List<String> values;
    private final boolean any;

    /** Return true if this selector fits provided value. */
    public boolean matches(Object value, boolean isCaseSensitive) {
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

    public boolean isNegative() {
        return negative;
    }
}
