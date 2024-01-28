/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
 */
package org.mdcfg;

import org.junit.BeforeClass;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.helpers.TestContextBuilder;
import org.mdcfg.provider.MdcProvider;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.mdcfg.helpers.Resources.YAML_PATH;
import static org.junit.Assert.assertEquals;

public class ConversionTest {

    private static MdcProvider provider;

    @BeforeClass
    public static void init() throws MdcException {
        provider = MdcBuilder.withYaml(YAML_PATH).build();
    }

    @Test
    public void testListProperty() throws MdcException {
        List<String> types = provider.getStringList(TestContextBuilder.EMPTY, "engine.type");
        assertEquals("[electric, gas, diesel]", types.toString());
    }

    @Test
    public void testDoubleProperty() throws MdcException {
        Double horsepower = provider.getDouble(TestContextBuilder.EMPTY, "horsepower");
        assertEquals(Double.valueOf(400d), horsepower);
    }

    @Test
    public void testMapProperty() throws MdcException {
        Map<String, Integer> productionModels = provider.getIntegerMap(TestContextBuilder.init().model("toyota").build(),
                "production-models");
        assertEquals(Integer.valueOf(2020), productionModels.get("Corolla Cross"));
    }
}
