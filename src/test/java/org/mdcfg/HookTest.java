/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg;

import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcContext;
import org.mdcfg.provider.MdcProvider;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.mdcfg.helpers.Resources.YAML_PATH;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HookTest {

    @Test
    public void testHookToAllProps() throws MdcException {
        AtomicInteger count = new AtomicInteger();
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH)
                .autoReload()
                .loadHook((v) -> {
                    count.getAndIncrement();
                    return v + "_all";
                })
                .build();

        assertEquals(55, count.get());

        MdcContext context = new MdcContext();
        context.put("model", "bmw");

        assertTrue(provider.getStringOptional(context, "price").orElse("").contains("_all"));
        assertTrue(provider.getStringOptional(context, "available-colors").orElse("").contains("_all"));
        assertTrue(provider.getStringOptional(context, "horsepower").orElse("").contains("_all"));
        provider.stopAutoReload();
    }

    @Test
    public void testHookExactMatch() throws MdcException {
        AtomicInteger count = new AtomicInteger();
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH)
                .autoReload()
                .loadHook("price", v -> {
                    count.getAndIncrement();
                    return v + "0";
                })
                .build();

        assertEquals(7, count.get());

        MdcContext context = new MdcContext();
        context.put("model", "bmw");
        assertEquals(Integer.valueOf(450000), provider.getInteger(context, "price"));
        provider.stopAutoReload();
    }

    @Test
    public void testHookPattern() throws MdcException {
        AtomicInteger count = new AtomicInteger();
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH)
                .autoReload()
                .loadHook(Pattern.compile("^h.+r$"), v -> {
                    count.getAndIncrement();
                    return v + "0";
                })
                .build();

        assertEquals(12, count.get());

        MdcContext context = new MdcContext();
        context.put("model", "bmw");
        context.put("drive", "4WD");
        assertEquals(Integer.valueOf(5000), provider.getInteger(context, "horsepower"));
        provider.stopAutoReload();
    }

    @Test
    public void testTwoHooks() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH)
                .autoReload()
                .loadHook(Pattern.compile("^h.+r$"), v -> v + "0")
                .loadHook("horsepower", v -> v + "0")
                .build();

        MdcContext context = new MdcContext();
        context.put("model", "bmw");
        assertEquals(Integer.valueOf(48000), provider.getInteger(context, "horsepower"));
        provider.stopAutoReload();
    }
}
