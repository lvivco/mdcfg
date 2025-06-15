package org.mdcfg;

import org.junit.Test;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.mdcfg.helpers.Resources.YAML_SINGLE_PATH;

public class AutoReloadStreamTest {

    @Test(expected = MdcException.class)
    public void testAutoReloadNotSupportedForStream() throws MdcException, FileNotFoundException {
        MdcBuilder.withYaml(new FileInputStream(YAML_SINGLE_PATH))
                .autoReload()
                .build();
    }
}
