package com.kintvisuals.client.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public abstract class Module {
    private final String name;
    private boolean enabled;
    // Добавляем список настроек
    public final List<Setting> settings = new ArrayList<>();

    public Module(String name) {
        this.name = name;
    }

    // Метод для добавления настроек в модуль
    public void addSettings(Setting... settings) {
        this.settings.addAll(Arrays.asList(settings));
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
}