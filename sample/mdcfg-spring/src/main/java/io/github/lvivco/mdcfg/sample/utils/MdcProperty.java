package io.github.lvivco.mdcfg.sample.utils;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface MdcProperty {
    @AliasFor("property")
    String value() default "";

    @AliasFor("value")
    String property() default "";

    String fallBack() default "";
}
