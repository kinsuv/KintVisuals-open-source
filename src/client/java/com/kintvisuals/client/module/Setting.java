package com.kintvisuals.client.module;

public class Setting {
    public String name;
    public double min, max, value;
    public int color; // В формате HEX (например, 0xFF00FFFF)
    public Type type;

    public enum Type { SLIDER, COLOR, CHECKBOX }

    // Конструктор для ползунка
    public Setting(String name, double min, double max, double defaultValue) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.value = defaultValue;
        this.type = Type.SLIDER;
    }

    // Конструктор для цвета
    public Setting(String name, int defaultColor) {
        this.name = name;
        this.color = defaultColor;
        this.type = Type.COLOR;
    }
}