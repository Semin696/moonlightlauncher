package moonlight.client.addon.addons.hud;

import org.lwjgl.glfw.GLFW;
import moonlight.client.addon.Addon;
import moonlight.client.addon.Type;

public class TargetInfo extends Addon {

    public TargetInfo() {
        super("TargetInfo", GLFW.GLFW_KEY_N, Type.HUD);
    }

}
