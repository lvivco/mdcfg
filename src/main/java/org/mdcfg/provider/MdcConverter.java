/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.provider;

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
}
