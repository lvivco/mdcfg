package org.mdcfg;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.helpers.TestContextBuilder;
import org.mdcfg.provider.MdcProvider;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mdcfg.helpers.Resources.HOCON_PATH;

public class HoconProviderTest {

    private static MdcProvider provider;

    @BeforeClass
    public static void init() throws MdcException {
        provider = MdcBuilder.withHocon(HOCON_PATH).build();
    }

    @Test
    public void testSelectors() throws MdcException {
        List<String> availableColorsAny = provider.getStringList(
                TestContextBuilder.init()
                        .model("nissan").build(), "available-colors");
        assertEquals("red", availableColorsAny.get(0));

        List<String> availableColorsBmw = provider.getStringList(
                TestContextBuilder.init()
                        .model("bmw").build(), "available-colors");
        assertEquals("metallic", availableColorsBmw.get(3));

        List<String> availableColorsFordAny = provider.getStringList(
                TestContextBuilder.init()
                        .model("ford")
                        .category("sedan").build(), "available-colors");
        assertEquals("blue", availableColorsFordAny.get(1));

        List<String> availableColorsFordCrossover = provider.getStringList(
                TestContextBuilder.init()
                        .model("ford")
                        .category("crossover").build(), "available-colors");
        assertEquals("gray", availableColorsFordCrossover.get(3));

        int toyotaHorsepower = provider.getInteger(
                TestContextBuilder.init()
                        .model("toyota")
                        .drive("2WD")
                        .build(), "horsepower");
        assertEquals(300, toyotaHorsepower);
    }
}
