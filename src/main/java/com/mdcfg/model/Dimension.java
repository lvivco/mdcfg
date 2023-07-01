package com.mdcfg.model;

public class Dimension {
    private String name;
    private boolean isRange;
    private boolean isList;
    private boolean isNumeric;

    public Dimension(String name, boolean isRange, boolean isList, boolean isNumeric) {
        this.name = name;
        this.isRange = isRange;
        this.isList = isList;
        this.isNumeric = isNumeric;
    }

    public String getName() {
        return name;
    }

    public boolean isRange() {
        return isRange;
    }

    public boolean isList() {
        return isList;
    }

    public boolean isNumeric() {
        return isNumeric;
    }
}
