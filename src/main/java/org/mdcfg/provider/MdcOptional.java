package org.mdcfg.provider;

import org.mdcfg.exceptions.MdcException;
import org.mdcfg.utils.SilentFunction;

import java.util.Optional;
import java.util.function.Function;

public class MdcOptional {

    private final MdcProvider provider;

    public MdcOptional(MdcProvider provider) {
        this.provider = provider;
    }

    public Optional<String> getString(MdcContext context, String key){
        return ofNullable((p) -> p.getString(context, key));
    }

    public Optional<Boolean> getBoolean(MdcContext context, String key){
        return ofNullable((p) -> p.getBoolean(context, key));
    }

    public Optional<Float> getFloat(MdcContext context, String key){
        return ofNullable((p) -> p.getFloat(context, key));
    }

    public Optional<Double> getDouble(MdcContext context, String key){
        return ofNullable((p) -> p.getDouble(context, key));
    }

    public Optional<Short> getShort(MdcContext context, String key){
        return ofNullable((p) -> p.getShort(context, key));
    }

    public Optional<Integer> getInteger(MdcContext context, String key){
        return ofNullable((p) -> p.getInteger(context, key));
    }

    public Optional<Long> getLong(MdcContext context, String key){
        return ofNullable((p) -> p.getLong(context, key));
    }

    public <T> Optional<T> getValue(MdcContext context, String key, Function<String, T> converter){
        return ofNullable((p) -> p.getValue(context, key, converter));
    }

    private <R> Optional<R> ofNullable(SilentFunction<MdcProvider, R> func) {
        try {
            return Optional.ofNullable(func.apply(provider));
        } catch (MdcException e){
            return Optional.empty();
        }
    }
}
