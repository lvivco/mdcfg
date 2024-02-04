/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
 */
package org.mdcfg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.mdcfg.builder.MdcBuilder;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.helpers.TestContextBuilder;
import org.mdcfg.helpers.EnginePOJO;
import org.mdcfg.provider.MdcProvider;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mdcfg.helpers.Resources.*;

public class ProviderTest {

    private static MdcProvider provider;

    @BeforeClass
    public static void init() throws MdcException {
        provider = MdcBuilder.withYaml(YAML_PATH).build();
    }

    @Test
    public void testSubProperty() throws MdcException {
        String types = provider.getString(TestContextBuilder.EMPTY, "engine.type");
        assertEquals("[electric, gas, diesel]", types);

        String drives = provider.getString(TestContextBuilder.EMPTY, "engine.drive");
        assertEquals("[4WD, 2WD]", drives);
    }

    @Test
    public void testSelectors() throws MdcException {
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
        assertEquals("350", horsepowerFord4WD);

        String horsepowerFord2WD = provider.getString(TestContextBuilder.init().model("ford").drive("2WD").build(), "horsepower");
        assertEquals("300", horsepowerFord2WD);

        String horsepowerFiat2WD = provider.getString(TestContextBuilder.init().model("hyundai").drive("2WD").build(), "horsepower");
        assertEquals("400", horsepowerFiat2WD);
    }

    @Test
    public void testListSelectors() throws MdcException {
        String priceToyotaLeatherSeats = provider.getString(
                TestContextBuilder.init()
                        .model("toyota")
                        .addIn(List.of("leather-seats"))
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
                        .addIn(List.of("leather-seats"))
                        .build(), "price");
        assertEquals("55000", priceBmwLeatherSeats);

        String priceBmwPanoramicRoof = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addIn(List.of("panoramic-roof"))
                        .build(), "price");
        assertEquals("55000", priceBmwPanoramicRoof);

        String priceBmwLeatherSeatsPanoramicRoof = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addIn(Arrays.asList("panoramic-roof", "leather-seats"))
                        .build(), "price");
        assertEquals("55000", priceBmwLeatherSeatsPanoramicRoof);

        String priceBmwXenonLightsPanoramicRoof = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addIn(Arrays.asList("panoramic-roof", "xenon-lights"))
                        .build(), "price");
        assertEquals("55000", priceBmwXenonLightsPanoramicRoof);

        String priceBmwXenonLights = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addIn(List.of("xenon-lights"))
                        .build(), "price");
        assertEquals("45000", priceBmwXenonLights);
    }

    @Test
    public void testRangesAndNumericDimensions() throws MdcException {
        assertFalse(provider.getBoolean(TestContextBuilder.init().clearance(-5.0).build(), "off-road"));
        assertTrue(provider.getBoolean(TestContextBuilder.init().clearance(1000.0).build(), "off-road"));
        // check [!12..17, 19, 20] matches below
        assertTrue(provider.getBoolean(TestContextBuilder.init().clearance(14.0).build(), "off-road"));
        assertNull(provider.getBoolean(TestContextBuilder.init().clearance(18.0).build(), "off-road"));
        assertTrue(provider.getBoolean(TestContextBuilder.init().clearance(19.0).build(), "off-road"));
        assertTrue(provider.getBoolean(TestContextBuilder.init().clearance(20.0).build(), "off-road"));
    }

    @Test
    @SuppressWarnings("UNCHECKED_CAST")
    public void testCompoundMapProperty() throws MdcException {
        Map<String, Object> engine = provider.getCompoundMap(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),"engine");
        assertEquals("[electric, gas, diesel]",engine.get("type"));
        assertEquals("inline",((Map<String, Object>) engine.get("block")).get("type"));
    }

    @Test
    public void testCompoundJSONProperty() throws MdcException {
        String engine = provider.getCompoundJSON(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),"engine.block", false);
        assertEquals("{\"block\":{\"cylinder-count\":\"6\",\"type\":\"inline\"}}",engine);
    }

    @Test
    public void testCompoundObjectPropertyByClass() throws MdcException {
        EnginePOJO engine = provider.getCompoundObject(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),"engine", EnginePOJO.class);
        assertEnginePOJO(engine);
    }

    @Test
    public void testCompoundObjectPropertyByType() throws MdcException {
        JavaType type = new ObjectMapper().getTypeFactory().constructType(EnginePOJO.class);
        EnginePOJO engine = provider.getCompoundObject(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),"engine", type);
        assertEnginePOJO(engine);
    }

    @Test
    public void testCompoundObjectPropertyByTypeReference() throws MdcException {
        final TypeReference<EnginePOJO> typeReference = new TypeReference<>(){};
        EnginePOJO engine = provider.getCompoundObject(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),"engine", typeReference);
        assertEnginePOJO(engine);
    }

    @Test
    public void testStringPropertyResolver() throws MdcException {
        String label = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),"engine-info.label");
        assertEquals("Block inline Type 480 Horsepower", label);
    }

    @Test
    public void testListPropertyResolver() throws MdcException {
        List<String> list = provider.getStringList(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),"engine-info.list");
        assertEquals(2, list.size());
        assertEquals("inline", list.get(0));
        assertEquals("Cylinders: 6", list.get(1));
    }

    private static void assertEnginePOJO(EnginePOJO engine) {
        assertEquals("[electric, gas, diesel]", engine.getType());
        assertEquals("[4WD, 2WD]", engine.getDrive());
        assertEquals("inline", engine.getBlock().getType());
        assertEquals(6, engine.getBlock().getCylinderCount());
    }
}
