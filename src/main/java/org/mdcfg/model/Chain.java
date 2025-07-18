/**
 *   Copyright (C) 2025 LvivCoffeeCoders team.
 */
package org.mdcfg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.mdcfg.provider.MdcContext;

import java.util.Map;


/**
 *  Represents one config chain.
 *  <p> For example property:
 *  <pre>
 *    horsepower:
 *      any@: 400
 *      model@bmw:
 *        drive@4WD: 500
 *  </pre>
 *  contains two chains:
 *  <ul>
 *  <li>{@code model@any.drive@any}</li>
 *  <li>{@code model@bmw.drive@4WD}</li>
 *  </ul>
 *  Those chains will be represented in RegExps:
 *  <ul>
 *  <li>{@code model@.*\.drive@.*$}</li>
 *  <li>{@code model@bmw\.drive@4WD$}</li>
 *  </ul>
 */
@AllArgsConstructor
@ToString
@Getter
public class Chain {
    private final Map<String, Selector> selectors;
    private String value;

    /**
     * Check whether chain matches context.
     * <p>All positive selectors must match. The chain is discarded only when
     * every negative selector matches.</p>
     */
    public boolean match(MdcContext context, boolean isCaseSensitive) {
        boolean hasNegative = false;
        boolean allNegativesMatch = true;

        for (var entry : selectors.entrySet()) {
            Selector selector = entry.getValue();

            boolean raw = selector.rawMatch(context, isCaseSensitive);
            if (selector.isNegative()) {
                hasNegative = true;
                if (!raw) {
                    allNegativesMatch = false;
                }
            } else if (!raw) {
                return false;
            }
        }

        return !(hasNegative && allNegativesMatch);
    }


}
