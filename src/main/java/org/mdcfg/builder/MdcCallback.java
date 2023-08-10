package org.mdcfg.builder;

import lombok.Builder;

import java.util.function.Consumer;

@Builder
public class MdcCallback<T, U> {
    private Consumer<T> onSuccess;
    private Consumer<U> onFailure;

    public void success(T data){
        if(onSuccess != null){
            onSuccess.accept(data);
        }
    }

    public void fail(U data){
        if(onFailure != null){
            onFailure.accept(data);
        }
    }
}
