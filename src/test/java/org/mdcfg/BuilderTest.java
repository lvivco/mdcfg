/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
 */
package org.mdcfg;

import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.provider.MdcProvider;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mdcfg.helpers.Resources.*;

public class BuilderTest {

    @Test
    public void testYaml() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH).build();
        assertNotNull(provider);
        assertEquals(15, provider.getSize());
    }

    @Test
    public void testJson() throws MdcException {
        MdcProvider provider = MdcBuilder.withJson(JSON_PATH).build();
        assertNotNull(provider);
        assertEquals(1, provider.getSize());
    }

    @Test
    public void testHocon() throws MdcException {
        MdcProvider provider = MdcBuilder.withHocon(HOCON_PATH).build();
        assertNotNull(provider);
        assertEquals(6, provider.getSize());
    }

    @Test(expected = MdcException.class)
    public void failTest() throws MdcException {
        MdcBuilder.withYaml("wrong path").build();
    }
}
