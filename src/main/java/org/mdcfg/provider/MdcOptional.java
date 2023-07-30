package org.mdcfg.provider;

import java.util.Optional;
import java.util.function.Function;

public class MdcOptional {

    private final MdcProvider provider;

    public MdcOptional(MdcProvider provider) {
        this.provider = provider;
    }

    public Optional<String> getString(MdcContext context, String key){
        return Optional.ofNullable(provider.getString(context, key));
    }

    public Optional<Boolean> getBoolean(MdcContext context, String key){
        return Optional.ofNullable(provider.getBoolean(context, key));
    }

    public Optional<Float> getFloat(MdcContext context, String key){
        return Optional.ofNullable(provider.getFloat(context, key));
    }

    public Optional<Double> getDouble(MdcContext context, String key){
        return Optional.ofNullable(provider.getDouble(context, key));
    }

    public Optional<Short> getShort(MdcContext context, String key){
        return Optional.ofNullable(provider.getShort(context, key));
    }

    public Optional<Integer> getInteger(MdcContext context, String key){
        return Optional.ofNullable(provider.getInteger(context, key));
    }

    public Optional<Long> getLong(MdcContext context, String key){
        return Optional.ofNullable(provider.getLong(context, key));
    }

    public <T> Optional<T> getValue(MdcContext context, String key, Function<String, T> converter){
        return Optional.ofNullable(provider.getValue(context, key, converter));
    }
}
