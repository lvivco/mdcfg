/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *  Represents one config dimension.
 *  <p> For example property:
 *  <pre>
 *    horsepower:
 *      any@: 400
 *      model@bmw:
 *        drive@4WD: 500
 *  </pre>
 *  contains two dimensions:
 *  <ul>
 *  <li>{@code model}</li>
 *  <li>{@code drive}</li>
 *  </ul>
 *
 *  <p> Could be numeric:
 *  <pre>
 *    offroad:
 *      clearance@8: false
 *  </pre>
 *
 *  <p> Could be numeric range:
 *  <pre>
 *    offroad:
 *      clearance@[10..12]: false
 *  </pre>
 *
 *  <p> Could be list (represented by "*" in config and passed as List in conf context):
 *  <pre>
 *    price:
 *      addin*@panoramic-roof: 55000
 *  </pre>
 */
@Data
@AllArgsConstructor
public class Dimension {
    private String name;
    private boolean isRange;
    private boolean isList;
    private boolean isNumeric;
}
