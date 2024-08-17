/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.helpers.TestContextBuilder;
import org.mdcfg.provider.MdcProvider;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mdcfg.helpers.Resources.YAML_PATH;

public class AliasTest {

    private static MdcProvider provider;

    @BeforeClass
    public static void init() throws MdcException {
        provider = MdcBuilder.withYaml(YAML_PATH).build();
    }

    @Test
    public void testAlias() throws MdcException {
        String colorsX5 = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .category("crossover")
                        .build(), "available-colors");
        assertEquals("[white, black, blue]", colorsX5);
    }

    @Test
    public void testListAlias() throws MdcException {
        String priceToyotaCruiseControl = provider.getString(
                TestContextBuilder.init()
                        .model("toyota")
                        .addIn(List.of("cruise-control"))
                        .build(), "price");
        assertEquals("37000", priceToyotaCruiseControl);
    }
}
