/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.provider;

import org.mdcfg.exceptions.MdcException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MdcConverter {
    private MdcConverter() {}

    public static final UnaryOperator<String> TO_STRING = v -> v;
    public static final Function<String, Boolean> TO_BOOLEAN = Boolean::parseBoolean;
    public static final Function<String, Short> TO_SHORT = Short::parseShort;
    public static final Function<String, Integer> TO_INTEGER = Integer::parseInt;
    public static final Function<String, Long> TO_LONG = Long::parseLong;
    public static final Function<String, Float> TO_FLOAT = Float::parseFloat;
    public static final Function<String, Double> TO_DOUBLE = Double::parseDouble;

    private static final Map<String, Function<String, ?>> CONVERTERS;
    
    static {
        Map<String, Function<String, ?>> map = new HashMap<>();
        map.put("java.lang.String", TO_STRING);
        map.put("boolean", TO_BOOLEAN);
        map.put("java.lang.Boolean", TO_BOOLEAN);
        map.put("short", TO_SHORT);
        map.put("java.lang.Short", TO_SHORT);
        map.put("int", TO_INTEGER);
        map.put("java.lang.Integer", TO_INTEGER);
        map.put("long", TO_LONG);
        map.put("java.lang.Long", TO_LONG);
        map.put("float", TO_FLOAT);
        map.put("java.lang.Float", TO_FLOAT);
        map.put("double", TO_DOUBLE);
        map.put("java.lang.Double", TO_DOUBLE);
        CONVERTERS = Map.copyOf(map);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertScalar(Class<T> clas, String value) throws MdcException {
        Function<String, ?> converter = CONVERTERS.get(clas.getCanonicalName());
        if (converter == null) {
            throw new MdcException(String.format("Provided class: %s is not scalar or supported", clas.getCanonicalName()));
        }
        return (T) converter.apply(value);
    }
}
