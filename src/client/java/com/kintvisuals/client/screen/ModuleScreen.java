package com.kintvisuals.client.screen;

import com.kintvisuals.client.module.Module;
import com.kintvisuals.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModuleScreen extends Screen {

    public ModuleScreen() {
        super(Text.literal("KintVisuals Modules"));
    }

    @Override
    protected void init() {
        int y = 40;

        for (Module module : ModuleManager.getModules()) {
            addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
                    Text.literal(module.getName() + ": " + (module.isEnabled() ? "ON" : "OFF")),
                    btn -> {
                        module.toggle();
                        btn.setMessage(Text.literal(
                                module.getName() + ": " + (module.isEnabled() ? "ON" : "OFF")
                        ));
                    }
            ).dimensions(width / 2 - 100, y, 200, 20).build());

            y += 25;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }
}
