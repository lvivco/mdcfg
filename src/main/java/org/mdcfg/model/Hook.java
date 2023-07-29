package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

@EqualsAndHashCode
@AllArgsConstructor
public class Hook {
    @Getter
    private final Pattern pattern;
    @Getter
    private final UnaryOperator<String> function;
}
