/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.provider;

import lombok.ToString;

import java.util.HashMap;

/**
 * Map that is used to pass context for reading property value where key is selector name and value is selector value.
 * <p> Example:
 * <pre>
 *  horsepower:
 *   model@bmw:500
 * </pre>
 * Selector "model" should be passed in context as key and "bmw" as value.
 */
@ToString(callSuper = true)
public class MdcContext extends HashMap<String, Object>{
}
