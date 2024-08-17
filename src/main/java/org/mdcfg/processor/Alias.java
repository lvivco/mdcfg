/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.processor;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.regex.Pattern;

/**
 *  Helper class for holding alias.
 *  <p> For example alias and property:
 *  <pre>
 *    aliases:
 *      model@bmw:
 *        cat@crossover: line@x5
 *
 *    horsepower:
 *      any@: 400
 *      line@x5: 500
 *  </pre>
 *  after processing final config will be following:
 *  <pre>
 *    horsepower:
 *      any@: 400
 *      model@bmw:
 *        cat@crossover: 500
 *  </pre>
 */
@Data
@AllArgsConstructor
public class Alias {
    private String targetDimension;
    private Pattern from;
    private String to;
}
