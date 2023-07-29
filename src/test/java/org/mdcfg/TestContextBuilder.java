package org.mdcfg;

import org.mdcfg.provider.MdcContext;

import java.util.List;

public class TestContextBuilder {
    private static String MODEl = "model";
    private static String CATEGORY = "cat";
    private static String DRIVE = "drive";
    private static String ADDIN = "addin";
    private static String CLEARANCE = "clearance";

    public static MdcContext EMPTY = new MdcContext();

    private MdcContext ctx;

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

    public TestContextBuilder addin(List<String> addin){
        ctx.put(ADDIN, addin);
        return this;
    }

    public TestContextBuilder clearance(Double clearance){
        ctx.put(CLEARANCE, clearance);
        return this;
    }
}
