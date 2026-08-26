package moonlight.client.handler;

import net.minecraft.client.MinecraftClient;

import org.lwjgl.glfw.GLFW;

import moonlight.Moonlight;
import moonlight.api.event.EventSubscribe;
import moonlight.api.event.events.EventKeyboard;
import moonlight.api.handler.other.KeyboardSystem;
import moonlight.client.addon.Addon;
import moonlight.client.gui.screens.clickGui.ClickGui;

public class Keyboard {

    private final KeyboardSystem keyboardSystem = new KeyboardSystem();

    @EventSubscribe
    public void keyboardHandler(EventKeyboard eventKeyboard) {
        if(MinecraftClient.getInstance().currentScreen != null) return;

        for(Addon addon : Moonlight.addonSystem.getModules()) {
            if(addon.getKey() == -1) continue;

            if(this.keyboardSystem.isKeyPress(addon.getKey()))
                addon.toggleModule(addon);
        }

        if(this.keyboardSystem.isKeyPress(GLFW.GLFW_KEY_RIGHT_SHIFT))
            MinecraftClient.getInstance().setScreen(new ClickGui());
    }

}
