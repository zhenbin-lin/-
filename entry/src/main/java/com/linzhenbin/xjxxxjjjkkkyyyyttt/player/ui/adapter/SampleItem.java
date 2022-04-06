package com.linzhenbin.xjxxxjjjkkkyyyyttt.player.ui.adapter;

import java.io.Serializable;

public class SampleItem implements Serializable {

    public String value;
    public String key;

    public SampleItem(String url, String name) {
        value = url;
        key = name;
    }

    public SampleItem() {
    }

    @Override
    public String toString() {
        return "SampleItem{" +
                "value='" + value + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getKey(){
        return key;
    }
}
