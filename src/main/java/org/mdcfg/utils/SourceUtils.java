package org.mdcfg.utils;

import org.mdcfg.exceptions.MdcException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SourceUtils {
    private static final String SELECTOR_SEPARATOR= "@";
    private static final String DIMENSION_SEPARATOR = ":";
    private static final String ANY = "any";
    private static final Pattern PROPERTY_PATTERN = Pattern.compile(":[^:]*[@].*");

    private SourceUtils() {}

    public static Map<String, Object> flatten(Map<String, Object> map) {
        return flatten(map, "");
    }

    public static Map<String, Map<String, String>> collectProperties(Map<String, Object> rawData) throws MdcException {
        Map<String, Map<String, String>> data = new HashMap<>();
        for(var entry:rawData.entrySet()) {
            String key = entry.getKey();
            if(key.contains(SELECTOR_SEPARATOR)){
                Matcher matcher = PROPERTY_PATTERN.matcher(key);
                if(!matcher.find()){
                    throw new MdcException(String.format("Invalid property %s", key));
                }
                int index = matcher.start();
                Map<String, String> prop = getProperty(data, key.substring(0, index));
                prop.put(key.substring(index + 1), entry.getValue().toString());
            } else {
                Map<String, String> prop = getProperty(data, key);
                prop.put(ANY, entry.getValue().toString());
            }
        }
        return data;
    }

    public static Pair<String, String> splitSelector(String selector){
        String[] split = selector.split(SELECTOR_SEPARATOR);
        String key = split[0];
        String value = split.length > 1 ? split[1] : null;
        return Pair.of(key, value);
    }

    private static Map<String, Object> flatten(Map<String, Object> map, String prefix) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.putAll(flatten((Map<String, Object>) value,prefix + key + DIMENSION_SEPARATOR));
            } else {
                result.put(prefix + key, value);
            }
        }
        return result;
    }

    private static Map<String, String> getProperty(Map<String, Map<String, String>> data, String key){
        if(data.containsKey(key)){
            return data.get(key);
        }
        Map<String, String> val = new LinkedHashMap<>();
        data.put(key, val);
        return val;
    }

}
