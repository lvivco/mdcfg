/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.exceptions;

/**
 * General exception class for any fail events that can happened.
 */
public class MdcException extends Exception {

    /**
     * @see Exception#Exception(String)
     */
    public MdcException(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public MdcException(String message, Throwable cause) {
        super(message, cause);
    }
}
