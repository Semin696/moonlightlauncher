package moonlight.client.gui.widget;

import net.minecraft.client.gui.DrawContext;

import moonlight.client.gui.IGui;
import moonlight.client.gui.IStyle;

public class Widget implements IGui, IStyle {

    public final String name;

    public float x, y, width, height;

    public Widget(String name) {
        this.name = name;
    }

    @Override
    public void render(DrawContext drawContext, float x, float y, float deltaTime) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(DrawContext drawContext, float x, float y, float width, float height, float deltaTime) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

}
