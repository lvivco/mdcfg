/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package io.github.lvivco.mdcfg.sample.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface MdcProperty {
    String value() default "";

    String property() default "";

    String fallBack() default "";
}