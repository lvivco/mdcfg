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

public class CaseSensitiveKeyTest {

    private static MdcProvider provider;

    @BeforeClass
    public static void init() throws MdcException {
        provider = MdcBuilder.withYaml(YAML_PATH).caseSensitive().build();
    }

    @Test(expected = MdcException.class)
    public void testCaseSensitiveKeyNotFound() throws MdcException {
        provider.getStringList(TestContextBuilder.EMPTY, "engine.Type");
    }

    @Test
    public void testCaseSensitiveKey() throws MdcException {
        List<String> types = provider.getStringList(TestContextBuilder.EMPTY, "engine.type");
        assertEquals("[electric, gas, diesel]", types.toString());
    }

    @Test
    public void testCaseSensitiveKeyWithDimensions() throws MdcException {
        List<String> colorsFord = provider.getStringList(TestContextBuilder.init().model("Ford").category("crossover").build(), "available-colors");
        assertEquals("[white, black]", colorsFord.toString());

        List<String> colors = provider.getStringList(TestContextBuilder.init().model("ford").category("crossover").build(), "available-colors");
        assertEquals("[white, black, metalic, gray]", colors.toString());

        String horsepower = provider.getString(TestContextBuilder.init().model("bmw").drive("4WD").build(), "horsepower");
        assertEquals("500", horsepower);

        String horsepower4dw = provider.getString(TestContextBuilder.init().model("bmw").drive("4wd").build(), "horsepower");
        assertEquals("480", horsepower4dw);
    }
}
