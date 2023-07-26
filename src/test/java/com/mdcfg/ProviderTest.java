package com.mdcfg;

import com.mdcfg.builder.MdcBuilder;
import com.mdcfg.exceptions.MdcException;
import com.mdcfg.provider.MdcProvider;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static com.mdcfg.Resources.*;

public class ProviderTest {

    @Test
    public void testSubProperty() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH).build();

        String types = provider.getString(TestContextBuilder.EMPTY, "engine.type");
        assertEquals("[electric, gas, diesel]", types);

        String drives = provider.getString(TestContextBuilder.EMPTY, "engine.drive");
        assertEquals("[4WD, 2WD]", drives);
    }

    @Test
    public void testSelectors() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH).build();

        String horsepowerAny = provider.getString(TestContextBuilder.init().model("nissan").build(), "horsepower");
        assertEquals("400", horsepowerAny);

        String horsepowerFiat = provider.getString(TestContextBuilder.init().model("fiat").build(), "horsepower");
        assertEquals("380", horsepowerFiat);

        String horsepowerBmw4Wd = provider.getString(TestContextBuilder.init().model("bmw").drive("4WD").build(), "horsepower");
        assertEquals("500", horsepowerBmw4Wd);

        String horsepowerBmw2Wd = provider.getString(TestContextBuilder.init().model("bmw").drive("2WD").build(), "horsepower");
        assertEquals("480", horsepowerBmw2Wd);

        String horsepowerBmw = provider.getString(TestContextBuilder.init().model("bmw").build(), "horsepower");
        assertEquals("480", horsepowerBmw);

        String horsepowerToyota4WD = provider.getString(TestContextBuilder.init().model("toyota").drive("4WD").build(), "horsepower");
        assertEquals("350", horsepowerToyota4WD);

        String horsepowerToyota2WD = provider.getString(TestContextBuilder.init().model("toyota").drive("2WD").build(), "horsepower");
        assertEquals("300", horsepowerToyota2WD);

        String horsepowerFord4WD = provider.getString(TestContextBuilder.init().model("ford").drive("4WD").build(), "horsepower");
        assertEquals("350", horsepowerToyota4WD);

        String horsepowerFord2WD = provider.getString(TestContextBuilder.init().model("ford").drive("2WD").build(), "horsepower");
        assertEquals("300", horsepowerToyota2WD);

        String horsepowerFiat2WD = provider.getString(TestContextBuilder.init().model("Fiat").drive("2WD").build(), "horsepower");
        assertEquals("400", horsepowerFiat2WD);
    }

    @Test
    public void testListSelectors() throws MdcException {
        MdcProvider provider = MdcBuilder.withYaml(YAML_PATH).build();

        String priceToyotaLeatherSeats = provider.getString(
                TestContextBuilder.init()
                        .model("toyota")
                        .addin(Arrays.asList("leather-seats"))
                        .build(), "price");
        assertEquals("35000", priceToyotaLeatherSeats);

        String priceToyota = provider.getString(
                TestContextBuilder.init()
                        .model("toyota")
                        .build(), "price");
        assertEquals("30000", priceToyota);

        String priceBmwLeatherSeats = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addin(Arrays.asList("leather-seats"))
                        .build(), "price");
        assertEquals("55000", priceBmwLeatherSeats);

        String priceBmwPanoramicRoof = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addin(Arrays.asList("panoramic-roof"))
                        .build(), "price");
        assertEquals("55000", priceBmwPanoramicRoof);

        String priceBmwLeatherSeatsPanoramicRoof = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addin(Arrays.asList("panoramic-roof, leather-seats"))
                        .build(), "price");
        assertEquals("55000", priceBmwLeatherSeatsPanoramicRoof);

        String priceBmwXenonLightsPanoramicRoof = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addin(Arrays.asList("panoramic-roof, xenon-lights"))
                        .build(), "price");
        assertEquals("55000", priceBmwXenonLightsPanoramicRoof);

        String priceBmwXenonLights = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addin(Arrays.asList("xenon-lights"))
                        .build(), "price");
        assertEquals("45000", priceBmwXenonLights);
    }
}
