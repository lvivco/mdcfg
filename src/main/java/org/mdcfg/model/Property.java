package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mdcfg.provider.MdcContext;
import org.mdcfg.utils.ProviderUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class Property {

    @Getter
    private final String name;
    private final Map<String, Dimension> dimensions;
    private final List<Chain> chains;

    public String getString(MdcContext context) {
        String compare = createCompareString(context);
        for (Chain chain : chains) {
            if(chain.match(context, compare)){
                return chain.getValue();
            }
        }
        return null;
    }

    public String createCompareString(Map<String, Object> context) {
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
                    object = "[" + StringUtils.join(list, ',') + "]";
                }
            }
            compare.append(object);
        }
        return compare.toString();
    }
}
