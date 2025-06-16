/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.utils;

import org.mdcfg.exceptions.MdcException;
import org.apache.commons.lang3.tuple.Pair;
import org.mdcfg.model.Config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SourceUtils {
    private static final String SELECTOR_SEPARATOR= "@";
    private static final String DIMENSION_SEPARATOR = ":";
    private static final String ANY = "any";
    private static final Pattern PROPERTY_PATTERN = Pattern.compile(":[^:]*@.*");

    private SourceUtils() {}

    /**
     * Flatten configuration List based structure separated by ":".
     * <p> Example:
     * <pre>
     *  horsepower:
     *    any@: 400
     *    model@bmw:
     *      drive@4WD: 500
     *  </pre>
     *  will be flattened to:
     *  <ul>
     *  <li>{@code horsepower:any@: 400}</li>
     *  <li>{@code horsepower:model@bmw:drive@4WD: 500}</li>
     *  </ul>
     */
    public static Map<String, Object> flatten(Map<String, Object> map,
                                             Config config) {
        return flatten(map, "", config);
    }

    /** Get configuration grouped by properties */
    public static Map<String, Map<String, String>> collectProperties(Map<String, Object> rawData) throws MdcException {
        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        for(var entry:rawData.entrySet()) {
            String key = entry.getKey();
            if(key.contains(SELECTOR_SEPARATOR)){
                Pair<String, String> propertyMap = splitProperty(key);
                Map<String, String> prop = getProperty(data, propertyMap.getKey());
                prop.put(propertyMap.getValue(), entry.getValue().toString());
            } else {
                Map<String, String> prop = getProperty(data, key);
                prop.put(ANY, entry.getValue().toString());
            }
        }
        return data;
    }

    /** Split property {@code prop1:prop2:sel@val} to property key {@code prop1:prop2} and selector values {@code sel@val}*/
    public static Pair<String, String> splitProperty(String key) throws MdcException {
        Matcher matcher = PROPERTY_PATTERN.matcher(key);
        if(!matcher.find()){
            throw new MdcException(String.format("Invalid property %s", key));
        }
        int index = matcher.start();
        return Pair.of(key.substring(0, index), key.substring(index + 1));
    }

    /** Split selector {@code sel@val} to selector name and selector value */
    public static Pair<String, String> splitSelector(String selector){
        String[] split = selector.split(SELECTOR_SEPARATOR);
        String key = split[0];
        String value = split.length > 1 ? split[1] : null;
        return Pair.of(key, value);
    }

    /** replace only specified RegExp group with specified string */
    public static String replaceGroup(Pattern regexp, int groupIndex, String source, String replacement){
        Matcher matcher = regexp.matcher(source);
        if(matcher.find()) {
            int start = matcher.start(groupIndex);
            int end = matcher.end(groupIndex);
            source = source.substring(0, start) + replacement + source.substring(end);
        }
        return source;
    }
    private static Map<String, Object> flatten(Map<String, Object> map,
                                               String prefix,
                                               Config config) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = adjustCase(entry.getKey(), config);
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.putAll(flatten((Map<String, Object>) value,
                        prefix + key + DIMENSION_SEPARATOR,
                        config));
            } else {
                result.put(prefix + key, value);
            }
        }
        return result;
    }

    private static String adjustCase(String source,
                                     Config config) {
        int idx = source.indexOf(SELECTOR_SEPARATOR);
        if (idx >= 0) {
            String dimension = source.substring(0, idx);
            String value = source.substring(idx + 1);
            if (!config.isKeySensitive()) {
                dimension = dimension.toLowerCase(Locale.ROOT);
            }
            if (!config.isSelectorSensitive()) {
                value = value.toLowerCase(Locale.ROOT);
            }
            return dimension + SELECTOR_SEPARATOR + value;
        }
        return config.isKeySensitive() ? source : source.toLowerCase(Locale.ROOT);
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
