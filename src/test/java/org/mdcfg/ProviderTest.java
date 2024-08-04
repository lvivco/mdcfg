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
import org.mdcfg.helpers.*;
import org.mdcfg.provider.MdcProvider;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mdcfg.helpers.Resources.*;

public class ProviderTest {

    private static final TypeReference<EnginePOJO> ENGINE_TYPE_REFERENCE = new TypeReference<>(){};
    private static final TypeReference<BlockPOJO> BLOCK_TYPE_REFERENCE = new TypeReference<>(){};

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

        String priceBmw = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .addIn(List.of("panoramic-roof, xenon-lights"))
                        .build(), "price");
        assertEquals("45000", priceBmw);

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
        assertFalse(provider.getBoolean(TestContextBuilder.init().clearance(5d).build(), "off-road"));
        assertFalse(provider.getBoolean(TestContextBuilder.init().clearance(21d).build(), "off-road"));
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
        assertEquals("{\"cylinder-count\":\"6\",\"type\":\"inline\"}",engine);
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
        EnginePOJO engine = provider.getCompoundObject(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),"engine", ENGINE_TYPE_REFERENCE);
        assertEnginePOJO(engine);
    }

    @Test
    public void testStringPropertyResolver() throws MdcException {
        String label = provider.getString(
                TestContextBuilder.init()
                        .model("bmw")
                        .year("2024")
                        .build(),"engine-info.label");
        assertEquals("Block inline Type 480 Horsepower Year 2024 with ${outer_pattern}", label);
    }

    @Test
    public void testListPropertyResolver() throws MdcException {
        List<String> list = provider.getStringList(
                TestContextBuilder.init()
                        .model("bmw")
                        .year("2024")
                        .build(),"engine-info.list");
        assertEquals(4, list.size());
        assertEquals("inline", list.get(0));
        assertEquals("Cylinders: 6", list.get(1));
        assertEquals("2024", list.get(2));
        assertEquals("${outer_pattern}", list.get(3));
    }

    @Test
    public void testCompoundObjectListByClass() throws MdcException {
        Function<String, Class<? extends Object>> classResolver = k -> {
            if(k.equals("engine.block")) {
                return BlockPOJO.class;
            }
            return EnginePOJO.class;
        };
        List<Object> list = provider.getCompoundObjectListByClass(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),
                "engine-info.objects",
                classResolver);

        assertListPOJO(list);
    }

    @Test
    public void testCompoundObjectListByType() throws MdcException {
        Function<String, JavaType> typeResolver = k -> {
            if(k.equals("engine.block")) {
                return new ObjectMapper().getTypeFactory().constructType(BlockPOJO.class);
            }
            return new ObjectMapper().getTypeFactory().constructType(EnginePOJO.class);
        };
        List<?> list = provider.getCompoundObjectListByType(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),
                "engine-info.objects",
                typeResolver);

        assertListPOJO(list);
    }

    @Test
    public void testCompoundObjectListByTypeReference() throws MdcException {
        Function<String, TypeReference<? extends Object>> referenceResolver = k -> {
            if(k.equals("engine.block")) {
                return BLOCK_TYPE_REFERENCE;
            }
            return ENGINE_TYPE_REFERENCE;
        };
        List<Object> list = provider.getCompoundObjectListByTypeReference(
                TestContextBuilder.init()
                        .model("bmw")
                        .build(),
                "engine-info.objects",
                referenceResolver);

        assertListPOJO(list);
    }

    @Test(expected = MdcException.class)
    public void testPlainListException() throws MdcException {
        Function<String, JavaType> typeResolver = k -> null;
        List<?> list = provider.getCompoundObjectListByType(
                TestContextBuilder.EMPTY,
                "engine.type",
                typeResolver);
    }

    private static void assertListPOJO(List<?> list) {
        assertEquals(2, list.size());
        assertEquals(list.get(0).getClass(), EnginePOJO.class);
        assertEquals(list.get(1).getClass(), BlockPOJO.class);

        assertEnginePOJO((EnginePOJO)list.get(0));
        assertBlockPOJO((BlockPOJO)list.get(1));
    }

    private static void assertEnginePOJO(EnginePOJO engine) {
        assertEquals("[electric, gas, diesel]", engine.getType());
        assertEquals("[4WD, 2WD]", engine.getDrive());
        assertBlockPOJO(engine.getBlock());
    }

    private static void assertBlockPOJO(BlockPOJO block) {
        assertEquals("inline", block.getType());
        assertEquals(6, block.getCylinderCount());
    }
}
