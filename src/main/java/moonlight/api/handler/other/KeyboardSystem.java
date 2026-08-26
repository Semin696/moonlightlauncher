package moonlight.api.handler.other;

import net.minecraft.client.MinecraftClient;

import org.lwjgl.glfw.GLFW;

public class KeyboardSystem {

    private final long window = MinecraftClient.getInstance().getWindow().getHandle();

    private boolean isPressed = false;

    public boolean isKeyHold(int trigger) {
        return GLFW.glfwGetKey(window, trigger) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyPress(int trigger) {
        boolean isPressed = isKeyHold(trigger);

        if (isPressed && !this.isPressed) {
            this.isPressed = true;
            return true;
        } else if(!isPressed) {
            this.isPressed = false;
        }

        return false;
    }

}
