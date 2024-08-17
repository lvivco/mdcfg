/**
 *   Copyright (C) 2024 LvivCoffeeCoders team.
 */
package org.mdcfg.helpers;

import org.mdcfg.provider.MdcContext;

import java.util.List;

public class TestContextBuilder {
    private static final String MODEl = "model";
    private static final String CATEGORY = "cat";
    private static final String DRIVE = "drive";
    private static final String ADD_IN = "add-in";
    private static final String CLEARANCE = "clearance";
    private static final String YEAR = "year";

    public static MdcContext EMPTY = new MdcContext();

    private final MdcContext ctx;

    private TestContextBuilder() {
        ctx = new MdcContext();
    }

    public static TestContextBuilder init() {
        return new TestContextBuilder();
    }

    public MdcContext build(){
        return ctx;
    }

    public TestContextBuilder model(String model){
        ctx.put(MODEl, model);
        return this;
    }

    public TestContextBuilder category(String cat){
        ctx.put(CATEGORY, cat);
        return this;
    }

    public TestContextBuilder drive(String drive){
        ctx.put(DRIVE, drive);
        return this;
    }

    public TestContextBuilder addIn(List<String> addIn){
        ctx.put(ADD_IN, addIn);
        return this;
    }

    public TestContextBuilder clearance(Double clearance){
        ctx.put(CLEARANCE, clearance);
        return this;
    }

    public TestContextBuilder year(String year){
        ctx.put(YEAR, year);
        return this;
    }
}
