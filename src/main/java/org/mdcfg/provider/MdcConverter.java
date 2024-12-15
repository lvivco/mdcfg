/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.provider;

import org.mdcfg.exceptions.MdcException;

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

    @SuppressWarnings("unchecked")
    public static <T> T convertPrimitive(Class<T> clas, String value) throws MdcException {
        switch(clas.getCanonicalName()) {
            case "java.lang.String":
                return (T)TO_STRING.apply(value);
            case "boolean":
            case "java.lang.Boolean":
                return (T)TO_BOOLEAN.apply(value);
            case "short":
            case "java.lang.Short":
                return (T)TO_SHORT.apply(value);
            case "int":
            case "java.lang.Integer":
                return (T)TO_INTEGER.apply(value);
            case "long":
            case "java.lang.Long":
                return (T)TO_LONG.apply(value);
            case "float":
            case "java.lang.Float":
                return (T)TO_FLOAT.apply(value);
            case "double":
            case "java.lang.Double":
                return (T)TO_DOUBLE.apply(value);
            default:
                throw new MdcException(String.format("Provided class: %s is not primitive", clas.getCanonicalName()));
        }
    }
}
