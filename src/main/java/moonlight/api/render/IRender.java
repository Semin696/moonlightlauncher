package moonlight.api.render;

import net.minecraft.client.gui.DrawContext;

public interface IRender {
    default void render(DrawContext drawContext, float x, float y, float deltaTime) {}
    default void render(DrawContext drawContext, float x, float y, float width, float height, float deltaTime) {}
}
