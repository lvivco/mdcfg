/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcContext;
import org.mdcfg.utils.ProviderUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 *  Class that represents a property with chains.
 */
@AllArgsConstructor
public class Property {
    private static final char UNIT_SEPARATOR = (char) 31;

    @Getter private final String name;
    private final Map<String, Dimension> dimensions;
    private final List<Chain> chains;
    private final Map<String, List<Chain>> listChainMap;
    @Getter private final boolean hasReference;

    /** create compare string and match it on chains by down to up priority */
    public String getString(MdcContext context, boolean isCaseSensitive) {
        return getString(context, chains, null, null, isCaseSensitive);
    }

    /** Iterate by plitBy list context dimension creating compare string for each
     * and match it on active list chains by down to up priority. For example splitBy = addIn using following:<br/>
     * Context:
     * <pre>
     *   model = bmw
     *   addIn = [panoramic-roof, leather-seats]
     * </pre>
     * will be split into :
     * <pre>
     *   model = bmw
     *   addIn = panoramic-roof
     * </pre>
     * and
     * <pre>
     *   model = bmw
     *   addIn = leather-seats
     * </pre>
     * each of them will be used to get value
     */
    public List<String> getSplitString(MdcContext context, String splitBy, boolean isCaseSensitive) throws MdcException {
        List<?> list = Optional.ofNullable(context.get(splitBy))
                .map(ProviderUtils::toList)
                .orElseThrow(()-> new MdcException(String.format("Selector %s provided for split is not List", splitBy)));
        List<String> result = new ArrayList<>();
        List<Chain> activeChains = listChainMap.get(splitBy);
        if(activeChains != null) {
            for (Object splitValue : list) {
                String value = getString(context, activeChains, splitBy, splitValue, isCaseSensitive);
                if (value != null) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    /** create compare string using (plitBy@plitValue as replacement for List based context dimension)
     * and match it on active chains by down to up priority */
    private String getString(MdcContext context, List<Chain> activeChains, String splitBy, Object splitValue, boolean isCaseSensitive) {
        String compare = createCompareString(context, splitBy, splitValue);
        compare = isCaseSensitive ? compare : compare.toLowerCase(Locale.ROOT);
        for (Chain chain : activeChains) {
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
    private String createCompareString(Map<String, Object> context, String overrideName, Object overrideValue) {
        StringBuilder compare = new StringBuilder();
        for (Dimension dimension : dimensions.values()) {
            if(compare.length() > 0){
                compare.append(".");
            }
            compare.append(dimension.getName());
            compare.append("@");

            Object object;
            if(dimension.getName().equals(overrideName)) {
                object = "[" + overrideValue + "]";
            } else {
                object = context.getOrDefault(dimension.getName(), null);
                if (object != null) {
                    List<?> list = ProviderUtils.toList(object);
                    if (list != null) {
                        object = "[" + StringUtils.join(list, UNIT_SEPARATOR) + "]";
                    }
                }
            }
            compare.append(object);
        }
        return compare.toString();
    }
}
