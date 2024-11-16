/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import org.apache.commons.lang3.tuple.Pair;
import org.mdcfg.builder.MdcCallback;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.model.Property;
import org.mdcfg.processor.Processor;
import org.mdcfg.source.Source;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.mdcfg.provider.MdcConverter.*;

/**
 * Main Config class. Provides configuration by property name and context.
 */
public class MdcProvider {
    @FunctionalInterface
    private interface BiFunction<T, U, R> { R apply(T t, U u) throws MdcException; }

    private static final Pattern LIST_SIGN_PATTERN = Pattern.compile("[\\[\\]]");
    private static final Pattern SUB_PROPERTY_SEPARATOR = Pattern.compile("\\.");
    private static final String ROOT_PROPERTY = "^%s($|\\.)";
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private static final String REF_KEY_SEPARATOR = ":";
    private static final String REF_TYPE_MDC = "mdc";
    private static final String REF_TYPE_CTX = "ctx";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Processor processor;
    private final Source source;
    private final MdcCallback<Integer, MdcException> callback;
    private final boolean isCaseSensitive;

    private Map<String, Property> properties;

    /**
     * Creates configured provider object. Do not instantiate it directly, use {@link org.mdcfg.builder.MdcBuilder}
     *
     * @param source Config source
     * @param autoReload Flag that indicates whether config should autoreload on source change
     * @param reloadInterval interval in ms for reload
     * @param callback reload call back. See {@link MdcCallback}.
     * @param loadHooks List of functions that used for preprocessing config values.
     * @param isCaseSensitive Flag that indicates whether config should acknowledge case
     * @throws MdcException thrown in case something went wrong.
     */
    public MdcProvider(Source source, boolean autoReload, long reloadInterval, MdcCallback<Integer, MdcException> callback,
                       List<Hook> loadHooks, boolean isCaseSensitive) throws MdcException {
        this.source = source;
        this.callback = callback;
        this.isCaseSensitive = isCaseSensitive;

        this.processor = new Processor(loadHooks);

        readProperties();

        if(autoReload){
            source.observeChange(this::updateProperties, reloadInterval);
        }
    }

    /**
     * @return property count.
     */
    public int getSize() {
        return properties.size();
    }

    /**
     * Read property value and convert it to {@code String}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return property value or null.
     * @throws MdcException in case property not found.
     */
    public String getString(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_STRING);
    }

    /**
     * Read property value and convert it to {@code Optional<String>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional} of property value.
     */
    public Optional<String> getStringOptional(MdcContext context, String key){
        return getValueOptional(context, key, TO_STRING);
    }


    /**
     * Read property value and convert it to {@code Boolean}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return property value or null.
     * @throws MdcException in case property not found.
     */
    public Boolean getBoolean(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_BOOLEAN);
    }

    /**
     * Read property value and convert it to {@code Optional<Boolean>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional} of property value.
     */
    public Optional<Boolean>getBooleanOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_BOOLEAN);
    }

    /**
     * Read property value and convert it to {@code Float}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return property value or null.
     * @throws MdcException in case property not found.
     */
    public Float getFloat(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_FLOAT);
    }

    /**
     * Read property value and convert it to {@code Optional<Float>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional} of property value.
     */
    public Optional<Float> getFloatOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_FLOAT);
    }

    /**
     * Read property value and convert it to {@code Double}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return property value or null.
     * @throws MdcException in case property not found.
     */
    public Double getDouble(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_DOUBLE);
    }

    /**
     * Read property value and convert it to {@code Optional<Double>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional} of property value.
     */
    public Optional<Double> getDoubleOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_DOUBLE);
    }

    /**
     * Read property value and convert it to {@code Short}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return property value or null.
     * @throws MdcException in case property not found.
     */
    public Short getShort(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_SHORT);
    }

    /**
     * Read property value and convert it to {@code Optional<Short>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional} of property value.
     */
    public Optional<Short> getShortOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_SHORT);
    }

    /**
     * Read property value and convert it to {@code Integer}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return property value or null.
     * @throws MdcException in case property not found.
     */
    public Integer getInteger(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_INTEGER);
    }

    /**
     * Read property value and convert it to {@code Optional<Integer>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional} of property value.
     */
    public Optional<Integer> getIntegerOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_INTEGER);
    }

    /**
     * Read property value and convert it to {@code Long}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return property value or null.
     * @throws MdcException in case property not found.
     */
    public Long getLong(MdcContext context, String key) throws MdcException {
        return getValue(context, key, TO_LONG);
    }

    /**
     * Read property value and convert it to {@code Optional<Long>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional} of property value.
     */
    public Optional<Long> getLongOptional(MdcContext context, String key) {
        return getValueOptional(context, key, TO_LONG);
    }

    /**
     * Read property value and convert it to {@code List<String>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code List} of property values or null.
     * @throws MdcException in case property not found.
     */
    public List<String> getStringList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_STRING);
    }

    /**
     * Read property value and convert it to {@code Optional<List<String>>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional<List>} of property values or null.
     */
    public Optional<List<String>> getStringListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_STRING);
    }

    /**
     * Read property value and convert it to {@code List<Boolean>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code List} of property values or null.
     * @throws MdcException in case property not found.
     */
    public List<Boolean> getBooleanList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_BOOLEAN);
    }

    /**
     * Read property value and convert it to {@code Optional<List<Boolean>>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional<List>} of property values or null.
     */
    public Optional<List<Boolean>> getBooleanListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_BOOLEAN);
    }

    /**
     * Read property value and convert it to {@code List<Float>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code List} of property values or null.
     * @throws MdcException in case property not found.
     */
    public List<Float> getFloatList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_FLOAT);
    }

    /**
     * Read property value and convert it to {@code Optional<List<Float>>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional<List>} of property values or null.
     */
    public Optional<List<Float>> getFloatListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_FLOAT);
    }

    /**
     * Read property value and convert it to {@code List<Double>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code List} of property values or null.
     * @throws MdcException in case property not found.
     */
    public List<Double> getDoubleList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_DOUBLE);
    }

    /**
     * Read property value and convert it to {@code Optional<List<Double>>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional<List>} of property values or null.
     */
    public Optional<List<Double>> getDoubleListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_DOUBLE);
    }

    /**
     * Read property value and convert it to {@code List<Short>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code List} of property values or null.
     * @throws MdcException in case property not found.
     */
    public List<Short> getShortList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_SHORT);
    }

    /**
     * Read property value and convert it to {@code Optional<List<Short>>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional<List>} of property values or null.
     */
    public Optional<List<Short>> getShortListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_SHORT);
    }

    /**
     * Read property value and convert it to {@code List<Integer>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code List} of property values or null.
     * @throws MdcException in case property not found.
     */
    public List<Integer> getIntegerList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_INTEGER);
    }

    /**
     * Read property value and convert it to {@code Optional<List<Integer>>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional<List>} of property values or null.
     */
    public Optional<List<Integer>> getIntegerListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_INTEGER);
    }

    /**
     * Read property value and convert it to {@code List<Long>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code List} of property values or null.
     * @throws MdcException in case property not found.
     */
    public List<Long> getLongList(MdcContext context, String key) throws MdcException {
        return getValueList(context, key, TO_LONG);
    }

    /**
     * Read property value and convert it to {@code Optional<List<Long>>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Optional<List>} of property values or null.
     */
    public Optional<List<Long>> getLongListOptional(MdcContext context, String key) {
        return getValueListOptional(context, key, TO_LONG);
    }

    /**
     * Read property value and convert it to {@code Map<String,String>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Map} of property values or null.
     * @throws MdcException in case property not found.
     */
    public Map<String, String> getStringMap(MdcContext context, String key) throws MdcException {
        return getMap(context, key, TO_STRING, TO_STRING);
    }

    /**
     * Read property value and convert it to {@code Map<String,Double>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Map} of property values or null.
     * @throws MdcException in case property not found.
     */
    public Map<String, Double> getDoubleMap(MdcContext context, String key) throws MdcException {
        return getMap(context, key, TO_STRING, TO_DOUBLE);
    }

    /**
     * Read property value and convert it to {@code Map<String,Integer>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Map} of property values or null.
     * @throws MdcException in case property not found.
     */
    public Map<String, Integer> getIntegerMap(MdcContext context, String key) throws MdcException {
        return getMap(context, key, TO_STRING, TO_INTEGER);
    }

    /**
     * Read property value and convert it to {@code Map<String,Boolean>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Map} of property values or null.
     * @throws MdcException in case property not found.
     */
    public Map<String, Boolean> getBooleanMap(MdcContext context, String key) throws MdcException {
        return getMap(context, key, TO_STRING, TO_BOOLEAN);
    }

    /**
     * Read property value and convert it to {@code Map<String,Float>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Map} of property values or null.
     * @throws MdcException in case property not found.
     */
    public Map<String, Float> getFloatMap(MdcContext context, String key) throws MdcException {
        return getMap(context, key, TO_STRING, TO_FLOAT);
    }

    /**
     * Read property value and convert it to {@code Map<String,Long>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Map} of property values or null.
     * @throws MdcException in case property not found.
     */
    public Map<String, Long> getLongMap(MdcContext context, String key) throws MdcException {
        return getMap(context, key, TO_STRING, TO_LONG);
    }

    /**
     * Read property value and convert it to {@code Map<String,Short>}.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Map} of property values or null.
     * @throws MdcException in case property not found.
     */
    public Map<String, Short> getShortMap(MdcContext context, String key) throws MdcException {
        return getMap(context, key, TO_STRING, TO_SHORT);
    }

    /**
     * Read property value and convert it to provided type.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param converter {@code Function} that takes String and converts it to specified type.
     * @return property value or null.
     * @param <T> type in which value suppose to be converted
     * @throws MdcException  in case property not found.
     */
    public <T> T getValue(MdcContext context, String key, Function<String, T> converter) throws MdcException {
        Property property = getProperty(key);
        return Optional.ofNullable(getStringValue(property, context))
                .map(converter)
                .orElse(null);
    }

    /**
     * Read property value and convert it to {@code Optional} of provided type.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param converter {@code Function} that takes String and converts it to specified type.
     * @return {@code Optional} of property value.
     * @param <T> type in which value suppose to be converted
     */
    public <T> Optional<T> getValueOptional(MdcContext context, String key, Function<String, T> converter) {
        try {
            return Optional.ofNullable(getValue(context, key, converter));
        } catch (MdcException e) {
            return Optional.empty();
        }
    }

    /**
     * Iterate through list based dimension with provided name, read property value for each and convert it to provided type.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param splitBy name of List based dimension represented in context
     * @param converter {@code Function} that takes String and converts it to specified type.
     * @return List of iterated property values or empty list.
     * @param <T> type in which value suppose to be converted
     * @throws MdcException  in case property not found.
     */
    public <T> List<T> getSplitValue(MdcContext context, String key, String splitBy, Function<String, T> converter) throws MdcException {
        Property property = getProperty(key);
        return getSplitStringValue(property, context, splitBy).stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    /**
     * Read property value and convert it to {@code List} of provided type.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param converter {@code Function} that takes String and converts it to specified type.
     * @return {@code List} of property values or null.
     * @param <T> type in which each value in list suppose to be converted
     * @throws MdcException in case property not found.
     */
    public <T> List<T> getValueList(MdcContext context, String key, Function<String, T> converter) throws MdcException {
        Property property = getProperty(key);
        return Optional.ofNullable(getStringValue(property, context))
                .map(s -> LIST_SIGN_PATTERN.matcher(s).replaceAll(""))
                .map(s -> stringToList(converter, s))
                .orElse(null);
    }

    /**
     * Read property value and convert it to {@code Optional<List>} of provided type.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param converter {@code Function} that takes String and converts it to specified type.
     * @return {@code Optional<List>} of property value.
     * @param <T> type in which each value in list suppose to be converted
     */
    public <T> Optional<List<T>> getValueListOptional(MdcContext context, String key, Function<String, T> converter) {
        try {
            return Optional.ofNullable(getValueList(context, key, converter));
        } catch (MdcException e) {
            return Optional.empty();
        }
    }

    /**
     * Read property value and convert it to {@code List} of provided type.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param converter {@code Function} that takes String and converts it to specified type.
     * @return {@code List} of property values or null.
     * @param <T> type in which each value in list suppose to be converted
     * @throws MdcException in case property not found.
     */
    public <T> List<List<T>> getSplitValueList(MdcContext context, String key, String splitBy, Function<String, T> converter) throws MdcException {
        Property property = getProperty(key);
        return Optional.ofNullable(getSplitStringValue(property, context, splitBy))
                .map(l -> l.stream()
                        .map(s -> LIST_SIGN_PATTERN.matcher(s).replaceAll(""))
                        .map(s -> stringToList(converter, s))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Read property value and convert it to {@code Map} of provided type.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param keyConverter {@code Function} that converts key to specified type.
     * @param valueConverter {@code Function} that converts value to specified type.
     * @param <K> type in which each key in map suppose to be converted.
     * @param <V> type in which each value in map suppose to be converted.
     * @return {@code Map} of property values or null.
     * @throws MdcException in case property not found.
     */
    public <K, V> Map<K, V> getMap(MdcContext context, String key, Function<String, K> keyConverter, Function<String, V> valueConverter) throws MdcException {
        Property property = getProperty(key);
        String mapString = getStringValue(property, context);

        if(mapString == null){
            return null; //NOSONAR
        }
        if(StringUtils.isNotEmpty(mapString)) {
            return Arrays.stream(mapString.split("\n"))
                    .filter(StringUtils::isNotBlank)
                    .map(v -> v.split(":"))
                    .filter(v -> v.length == 2)
                    .collect(Collectors.toMap(v -> keyConverter.apply(v[0].trim()),
                            v -> valueConverter.apply(v[1].trim())));
        }
        return Collections.emptyMap();
    }

    /**
     * Read compound property and return result as Tree (based on maps).
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @return {@code Map} of property values.
     * @throws MdcException in case property not found.
     */
    public Map<String, Object> getCompoundMap(MdcContext context, String key) throws MdcException {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Property> propertyList = listCompoundProperty(key);
        for (Property property : propertyList) {
            String subKey = property.getName().substring(key.length());
            String[] path = SUB_PROPERTY_SEPARATOR.split(subKey);
            Map<String, Object> leaf = getLeaf(result, path);
            leaf.put(path[path.length-1], getStringValue(property, context));
        }
        return result;
    }

    /**
     * Read compound property and return result as JSON.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param prettify prettify response.
     * @return JSON string of property values.
     * @throws MdcException in case property not found.
     */
    public String getCompoundJSON(MdcContext context, String key, boolean prettify) throws MdcException {
        try {
            Map<String, Object> map = getCompoundMap(context, key);
            return prettify
                    ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(map)
                    : OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new MdcException("Couldn't deserialize object", e);
        }
    }

    /**
     * Read compound property and return result as POJO object.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param clas Class in which result suppose to be converted.
     * @param <T> type in which result suppose to be converted.
     * @return {@code Map} of property values.
     * @throws MdcException in case property not found.
     */
    public <T> T getCompoundObject(MdcContext context, String key, Class<T> clas) throws MdcException {
        Map<String, Object> map = getCompoundMap(context, key);
        return OBJECT_MAPPER.convertValue(map, clas);
    }

    /**
     * Read compound property and return result as POJO object.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param toValueType type in which result suppose to be converted.
     * @param <T> type in which result suppose to be converted.
     * @return {@code Map} of property values.
     * @throws MdcException in case property not found.
     */
    public <T> T getCompoundObject(MdcContext context, String key, JavaType toValueType) throws MdcException {
        Map<String, Object> map = getCompoundMap(context, key);
        return OBJECT_MAPPER.convertValue(map, toValueType);
    }

    /**
     * Read compound property and return result as POJO object.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param toValueTypeRef type reference in which result suppose to be converted.
     * @param <T> type in which result suppose to be converted.
     * @return {@code Map} of property values.
     * @throws MdcException in case property not found.
     */
    public <T> T getCompoundObject(MdcContext context, String key, TypeReference<T> toValueTypeRef) throws MdcException {
        Map<String, Object> map = getCompoundMap(context, key);
        return OBJECT_MAPPER.convertValue(map, toValueTypeRef);
    }

    /**
     * Read List that contains references to compound properties and return result as List of objects.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param classResolver Function that takes key and returns class in which result suppose to be converted.
     * @param <T> type in which result suppose to be converted.
     * @return {@code List} of property values.
     * @throws MdcException in case property not found or property doesn't contain references.
     */
    public <T> List<T> getCompoundObjectListByClass(MdcContext context, String key, Function<String, Class<? extends T>> classResolver) throws MdcException {
        return getCompoundObjectList(context, key, (ctx, k) -> getCompoundObject(context, k, classResolver.apply(k)));
    }

    /**
     * Read List that contains references to compound properties and return result as List of objects.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param typeResolver Function that takes key and returns type in which result suppose to be converted.
     * @param <T> type in which result suppose to be converted.
     * @return {@code List} of property values.
     * @throws MdcException in case property not found or property doesn't contain references.
     */
    public <T> List<T> getCompoundObjectListByType(MdcContext context, String key, Function<String, JavaType> typeResolver) throws MdcException {
        return getCompoundObjectList(context, key, (ctx, k) -> getCompoundObject(context, k, typeResolver.apply(k)));
    }

    /**
     * Read List that contains references to compound properties and return result as List of objects.
     *
     * @param context reading context {@link MdcContext}.
     * @param key property name.
     * @param typeRefResolver Function that takes key and returns type reference in which result suppose to be converted.
     * @param <T> type in which result suppose to be converted.
     * @return {@code List} of property values.
     * @throws MdcException in case property not found or property doesn't contain references.
     */
    public <T> List<T> getCompoundObjectListByTypeReference(MdcContext context, String key, Function<String, TypeReference<? extends T>> typeRefResolver) throws MdcException {
        return getCompoundObjectList(context, key, (ctx, k) -> getCompoundObject(context, k, typeRefResolver.apply(k)));
    }

    private <T> List<T> getCompoundObjectList(MdcContext context, String key, BiFunction<MdcContext, String, ? extends T> itemReader) throws MdcException {
        Property property = getProperty(key);
        String listString = Optional.ofNullable(property.getString(context, isCaseSensitive))
                .map(s -> LIST_SIGN_PATTERN.matcher(s).replaceAll(""))
                .orElse(null);

        if(listString == null){
            return null; //NOSONAR
        }
        if(!property.isHasReference()) {
            throw new MdcException(String.format("Value for key %s doesn't contain property references", key));
        }

        List<T> result = new ArrayList<>();
        if(StringUtils.isNotEmpty(listString)) {
            for (String k: COMMA_PATTERN.split(listString)) {
                String item = StringUtils.trim(k);
                Matcher m = REFERENCE_PATTERN.matcher(item);
                while (m.find()) {
                    Pair<String, String> ref = getRef(m.group(1));
                    if(ref != null && ref.getLeft().equals(REF_TYPE_MDC)) {
                        result.add(itemReader.apply(context, ref.getRight()));
                    }
                }
            }
        }
        return result;
    }

    private String processKey(String key) {
        return isCaseSensitive ? key : key.toLowerCase(Locale.ROOT);
    }

    private Property getProperty(String key) throws MdcException {
        return Optional.ofNullable(properties.get(processKey(key)))
                .orElseThrow(() -> new MdcException(String.format("Property %s not found.", key)));
    }

    private String getStringValue(Property property, MdcContext context) throws MdcException {
        String value = property.getString(context, isCaseSensitive);
        if(property.isHasReference()) {
            return processRefs(context, value);
        }
        return value;
    }

    private List<String> getSplitStringValue(Property property, MdcContext context, String splitBy) throws MdcException {
        List<String> values = property.getSplitString(context, splitBy, isCaseSensitive);
        if(property.isHasReference()) {
            for (int i = 0; i < values.size(); i++) {
                values.set(i, processRefs(context, values.get(i)));
            }
        }
        return values;
    }

    private String processRefs(MdcContext context, String value) throws MdcException {
        StringBuilder sb = new StringBuilder();
        Matcher m = REFERENCE_PATTERN.matcher(value);
        while (m.find()) {
            String refValue = getRefValue(context, m.group(1));
            if(refValue != null) {
                m.appendReplacement(sb, refValue);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private Pair<String, String> getRef(String refKeyGroup){
        int refKeyIndex = refKeyGroup.indexOf(REF_KEY_SEPARATOR);
        if(refKeyIndex > 1) {
            String refType = refKeyGroup.substring(0, refKeyIndex);
            String refKey = refKeyGroup.substring(refKeyIndex + 1);
            return Pair.of(refType, refKey);
        }
        return null;
    }

    private String getRefValue(MdcContext context, String refKeyGroup) throws MdcException {
        Pair<String, String> ref = getRef(refKeyGroup);
        if(ref != null) {
            switch (ref.getLeft()){
                case REF_TYPE_MDC:
                    return getString(context, ref.getRight());
                case REF_TYPE_CTX:
                    return getCtxStringValue(context, ref.getRight());
                default:
                    return null;
            }
        }
        return null;
    }

    private String getCtxStringValue( MdcContext context, String key) {
        if(context.containsKey(key)) {
            return String.valueOf(context.get(key));
        }
        return null;
    }

    private  List<Property> listCompoundProperty(String key) throws MdcException {
        final String keyPart = processKey(key);
        Pattern pattern = Pattern.compile(String.format(ROOT_PROPERTY, keyPart));
        // Could impact performance
        List<Property> result = properties.entrySet().stream()
                .filter(e -> pattern.matcher(e.getKey()).find())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        if(result.isEmpty()){
            throw new MdcException(String.format("Property %s not found.", key));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLeaf(Map<String, Object> root, String[] path) {
        Map<String, Object> leaf = root;
        if(path.length > 2) {
            for (int i = 1; i < path.length-1; i++) {
                if(!leaf.containsKey(path[i])){
                    leaf.put(path[i], new LinkedHashMap<>());
                }
                leaf = (Map<String, Object>) leaf.get(path[i]);
            }
        }
        return leaf;
    }


    private void updateProperties() {
        try {
            readProperties();
            Optional.ofNullable(callback).ifPresent(c->c.success(properties.size()));
        } catch (MdcException e) {
            Optional.ofNullable(properties).ifPresent(Map::clear);
            Optional.ofNullable(callback).ifPresent(c->c.fail(e));
        }
    }

    private void readProperties() throws MdcException {
        Map<String, Map<String, String>> data = source.read(processor::getIncludes, isCaseSensitive);
        properties = processor.process(data);
    }

    private static <T> List<T> stringToList(Function<String, T> converter, String listString) {
        if(StringUtils.isNotEmpty(listString)) {
            return Arrays.stream(COMMA_PATTERN.split(listString))
                    .map(StringUtils::trim)
                    .map(converter)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
