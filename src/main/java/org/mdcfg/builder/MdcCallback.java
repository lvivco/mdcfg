/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.builder;

import lombok.Builder;

import java.util.function.Consumer;

/**
 * Call back class that contains two consumers that will be called on success or failure event.
 * @param <T> Class of {@code onSuccess} argument
 * @param <U> Class of {@code onFailure} argument
 */
@Builder
public class MdcCallback<T, U> {
    private Consumer<T> onSuccess;
    private Consumer<U> onFailure;

    /**
     * Trigger success event.
     * @param data Object to be passed to {@code onSuccess} consumer
     */
    public void success(T data){
        if(onSuccess != null){
            onSuccess.accept(data);
        }
    }

    /**
     * Trigger failure event.
     * @param data Object to be passed to {@code onFailure} consumer
     */
    public void fail(U data){
        if(onFailure != null){
            onFailure.accept(data);
        }
    }
}
