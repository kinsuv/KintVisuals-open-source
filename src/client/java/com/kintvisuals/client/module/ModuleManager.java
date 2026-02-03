package com.kintvisuals.client.module;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private static final List<Module> MODULES = new ArrayList<>();

    public static void register(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getModules() {
        return MODULES;
    }

    public static <T extends Module> T get(Class<T> clazz) {
        for (Module module : MODULES) {
            if (clazz.isInstance(module)) {
                return clazz.cast(module);
            }
        }
        return null;
    }
}
