package org.mdcfg.model;

import lombok.AllArgsConstructor;

import org.mdcfg.provider.MdcContext;
import org.mdcfg.utils.ProviderUtils;

import java.util.List;
import org.mdcfg.model.Range;

/**
 * Represents single selector inside a chain. It knows
 * whether it is positive or negative and can match a context value.
 */
@AllArgsConstructor
public class Selector {
    private final boolean negative;
    private final boolean list;
    private final List<String> values;
    private final List<Range> ranges;

    /** Return true if this selector fits provided value and context. */
    public boolean matches(MdcContext context, Object value, boolean isCaseSensitive) {
        boolean match;
        if (!ranges.isEmpty()) {
            match = ranges.stream().anyMatch(r -> r.matches(context));
        } else {
            if (values.isEmpty()) {
                match = true;
            } else if (value == null) {
                match = false;
            } else {
                List<?> listVal = ProviderUtils.toList(value);
                if (list && listVal != null) {
                    match = false;
                    for (Object val : listVal) {
                        if (compare(val.toString(), isCaseSensitive)) {
                            match = true;
                            break;
                        }
                    }
                } else {
                    match = compare(value.toString(), isCaseSensitive);
                }
            }
        }

        return negative ? !match : match;
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
