package com.mdcfg.model;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Hook {
    private final Pattern pattern;
    private final Consumer<Chain> consumer;

    public Hook(Pattern pattern, Consumer<Chain> consumer) {
        this.pattern = pattern;
        this.consumer = consumer;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Consumer<Chain> getConsumer() {
        return consumer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hook hook = (Hook) o;
        return pattern.equals(hook.pattern) && consumer.equals(hook.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, consumer);
    }
}
