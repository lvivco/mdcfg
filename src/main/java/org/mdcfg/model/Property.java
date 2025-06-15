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
    Property enabled;

    /** check whether property contains enabled@ and if it returns true*/
    public boolean isEnabled(MdcContext context, boolean isCaseSensitive) {
        return enabled == null || Boolean.parseBoolean(enabled.getString(context, isCaseSensitive));
    }

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

    /** Check chains by down to up priority */
    private String getString(MdcContext context, List<Chain> activeChains, String splitBy, Object splitValue, boolean isCaseSensitive) {
        if(splitBy != null){
            context = new MdcContext() {{ putAll(context); put(splitBy, List.of(splitValue)); }};
        }
        for (Chain chain : activeChains) {
            if(chain.match(context, isCaseSensitive)){
                return chain.getValue();
            }
        }
        return null;
    }


}
