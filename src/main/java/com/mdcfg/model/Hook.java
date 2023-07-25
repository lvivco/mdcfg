package com.mdcfg.model;

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Hook {
    private final Pattern pattern;
    private final UnaryOperator<String> func;

    public Hook(Pattern pattern, UnaryOperator<String> func) {
        this.pattern = pattern;
        this.func = func;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public UnaryOperator<String> getFunction() {
        return func;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hook hook = (Hook) o;
        return pattern.equals(hook.pattern) && func.equals(hook.func);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, func);
    }
}
