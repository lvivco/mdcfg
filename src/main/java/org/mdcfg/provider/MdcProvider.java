/**
 *   Copyright (C) 2023 LvivCoffeeCoders team.
 */
package org.mdcfg.provider;

import org.mdcfg.builder.MdcCallback;
import org.mdcfg.exceptions.MdcException;
import org.mdcfg.model.Hook;
import org.mdcfg.model.Property;
import org.mdcfg.processor.Processor;
import org.mdcfg.source.Source;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Main Config class. Provides configuration by property name and context.
 */
public class MdcProvider {
    private static final UnaryOperator<String> TO_STRING = v -> v;
    private static final Function<String, Boolean> TO_BOOLEAN = Boolean::parseBoolean;
    private static final Function<String, Short> TO_SHORT = Short::parseShort;
    private static final Function<String, Integer> TO_INTEGER = Integer::parseInt;
    private static final Function<String, Long> TO_LONG = Long::parseLong;
    private static final Function<String, Float> TO_FLOAT = Float::parseFloat;
    private static final Function<String, Double> TO_DOUBLE = Double::parseDouble;
    private static final Pattern LIST_SIGN_PATTERN= Pattern.compile("[\\[\\]]");
    private static final Pattern SUB_PROPERTY_SEPARATOR = Pattern.compile("\\.");

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
        return Optional.ofNullable(property.getString(context, isCaseSensitive))
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
        String listString = Optional.ofNullable(property.getString(context, isCaseSensitive))
                .map(s -> LIST_SIGN_PATTERN.matcher(s).replaceAll(""))
                .orElse(null);

        if(listString == null){
            return null; //NOSONAR
        }
        if(StringUtils.isNotEmpty(listString)) {
            return Arrays.stream(listString.split(","))
                    .map(StringUtils::trim)
                    .map(converter)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
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
        String mapString = property.getString(context, isCaseSensitive);

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
        Map<String, Object> result = new HashMap<>();
        List<Property> propertyList = listCompoundProperty(key);
        for (Property property : propertyList) {
            String[] path = SUB_PROPERTY_SEPARATOR.split(property.getName());
            Map<String, Object> leaf = getLeaf(result, path);
            leaf.put(path[path.length-1], property.getString(context, isCaseSensitive));
        }
        return result;
    }

    private String processKey(String key) {
        return isCaseSensitive ? key : key.toLowerCase(Locale.ROOT);
    }

    private  Property getProperty(String key) throws MdcException {
        return Optional.ofNullable(properties.get(processKey(key)))
                .orElseThrow(() -> new MdcException(String.format("Property %s not found.", key)));
    }

    private  List<Property> listCompoundProperty(String key) throws MdcException {
        final String keyPart = processKey(key);
        List<Property> result = properties.entrySet().stream()
                .filter(e -> e.getKey().startsWith(keyPart))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        if(result.isEmpty()){
            throw new MdcException(String.format("Property %s not found.", key));
        }
        return result;
    }

    private Map<String, Object> getLeaf(Map<String, Object> root, String[] path) {
        Map<String, Object> leaf = root;
        if(path.length > 2) {
            for (int i = 1; i < path.length-1; i++) {
                if(!leaf.containsKey(path[i])){
                    leaf.put(path[i], new HashMap<>());
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
}
