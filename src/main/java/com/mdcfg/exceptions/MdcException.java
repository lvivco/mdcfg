package com.mdcfg.exceptions;

public class MdcException extends Exception{

    public MdcException(String message) {
        super(message);
    }

    public MdcException(String message, Throwable cause) {
        super(message, cause);
    }
}
