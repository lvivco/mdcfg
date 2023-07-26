package com.mdcfg;

import com.mdcfg.builder.MdcBuilder;
import com.mdcfg.exceptions.MdcException;
import com.mdcfg.provider.MdcProvider;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.mdcfg.Resources.*;

public class BuilderTest {

    @Test
    public void testYaml() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH).build();
        assertNotNull(provider);
        assertEquals(8, provider.getSize());
    }

    @Test
    public void testJson() throws MdcException {
        MdcProvider provider = MdcBuilder.withJson(JSON_PATH).build();
        assertNotNull(provider);
        assertEquals(1, provider.getSize());
    }

    @Test(expected = MdcException.class)
    public void failTest() throws MdcException {
        MdcBuilder.withYaml("wrong path").build();
    }
}
