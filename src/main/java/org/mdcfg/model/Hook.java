/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Helper class that contains property pattern and function to be called
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Hook {
    private final Pattern pattern;
    private final UnaryOperator<String> function;
}
