package com.fast0n.mediterraneabus.info;

public class DataInfo {

    private String name;
    private int icon;

    DataInfo(String name, int icon) {
        this.name = name;
        this.icon = icon;

    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

}