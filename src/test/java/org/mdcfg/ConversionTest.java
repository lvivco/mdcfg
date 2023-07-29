package org.mdcfg;

import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcProvider;
import org.junit.Test;

import java.util.List;

import static org.mdcfg.Resources.YAML_PATH;
import static org.junit.Assert.assertEquals;

public class ConversionTest {

    @Test
    public void testListProperty() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH).build();

        List<String> types = provider.getStringList(TestContextBuilder.EMPTY, "engine.type");
        assertEquals("[electric, gas, diesel]", types.toString());
    }

    @Test
    public void testDoubleProperty() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH).build();

        Double horsepower = provider.getDouble(TestContextBuilder.EMPTY, "horsepower");
        assertEquals(Double.valueOf(400d), horsepower);
    }
}
