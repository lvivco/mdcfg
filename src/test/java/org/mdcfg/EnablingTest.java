package org.mdcfg;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.helpers.TestContextBuilder;
import org.mdcfg.provider.MdcProvider;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mdcfg.helpers.Resources.YAML_PATH;

public class EnablingTest {

    private static MdcProvider provider;

    @BeforeClass
    public static void init() throws MdcException {
        provider = MdcBuilder.withYaml(YAML_PATH).caseSensitive().build();
    }

    @Test
    public void testEnabledProperty() {
        try {
            provider.getIntegerMap(TestContextBuilder.init().model("toyota").build(), "production-models");
        } catch (MdcException e) {
            fail(e.getClass().getSimpleName() + " was thrown");
        }
    }

    @Test(expected = MdcException.class)
    public void testDisabledProperty() throws MdcException {
        provider.getIntegerMap(TestContextBuilder.init().model("bmw").build(), "production-models");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCompoundDisabledProperty() throws MdcException {
        Map<String, Object> engine = provider.getCompoundMap(
                TestContextBuilder.init()
                        .model("ford")
                        .category("crossover")
                        .build(),"engine");
        assertFalse(((Map<String, Object>) engine.get("block")).containsKey("type"));
    }
}
