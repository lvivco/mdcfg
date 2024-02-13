/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mdcfg.provider.MdcContext;
import org.mdcfg.utils.ProviderUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *  Class that represents a property with chains.
 */
@AllArgsConstructor
public class Property {
    private static final char UNIT_SEPARATOR = (char) 31;

    @Getter private final String name;
    private final Map<String, Dimension> dimensions;
    private final List<Chain> chains;
    @Getter private final boolean hasReference;

    /** create compare string and match it on chains by down to up priority */
    public String getString(MdcContext context, boolean isCaseSensitive) {
        String compare = createCompareString(context);
        compare = isCaseSensitive ? compare : compare.toLowerCase(Locale.ROOT);
        for (Chain chain : chains) {
            if(chain.match(context, compare)){
                return chain.getValue();
            }
        }
        return null;
    }

    /**
     * Create compare string by dimensions order.
     * <p> For example for property:
     *  <pre>
     *    horsepower:
     *      any@: 400
     *      model@bmw:
     *        drive@4WD: 500
     *  </pre>
     *  Context:
     *  <pre>
     *   model = bmw
     *   drive = 4WD
     *  </pre>
     *  Compare string will be {@code model@bmw.drive@4WD}
     */
    private String createCompareString(Map<String, Object> context) {
        StringBuilder compare = new StringBuilder();
        for (Dimension dimension : dimensions.values()) {
            if(compare.length() > 0){
                compare.append(".");
            }
            compare.append(dimension.getName());
            compare.append("@");

            Object object = context.getOrDefault(dimension.getName(), null);
            if(object != null){
                List<?> list = ProviderUtils.toList(object);
                if(list != null){
                    object = "[" + StringUtils.join(list, UNIT_SEPARATOR) + "]";
                }
            }
            compare.append(object);
        }
        return compare.toString();
    }
}
