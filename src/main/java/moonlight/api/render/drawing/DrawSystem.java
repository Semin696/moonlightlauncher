package moonlight.api.render.drawing;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

import moonlight.api.render.drawing.builders.Builder;
import moonlight.api.render.drawing.builders.states.QuadColorState;
import moonlight.api.render.drawing.builders.states.QuadRadiusState;
import moonlight.api.render.drawing.builders.states.SizeState;
import moonlight.api.render.drawing.renderers.impl.*;

public class DrawSystem {

    public static void drawBlur(DrawContext drawContext, float x, float y, float width, float height, double radius1, double radius2, double radius3, double radius4, int color) {
        BuiltBlur blur = Builder.blur().size(new SizeState(width, height)).radius(new QuadRadiusState(radius1, radius2, radius3, radius4))
                .blurRadius(50).smoothness(1).color(new QuadColorState(color)).build();

        blur.render(drawContext.getMatrices().peek().getPositionMatrix(), x, y);
    }

    public static void drawBorder(DrawContext drawContext, float x, float y, float width, float height, double radius1, double radius2, double radius3, double radius4, int color) {
        BuiltBorder border = Builder.border().size(new SizeState(width, height)).color(new QuadColorState(color)).
                radius(new QuadRadiusState(radius1, radius2, radius3, radius4)).thickness(0.3f).smoothness(0.5f, 0.5f).build();

        border.render(drawContext.getMatrices().peek().getPositionMatrix(), x, y);
    }

    public static void drawBloom(DrawContext drawContext, float x, float y, float width, float height, float radius, float smoothness, float blurRadius, int color) {
        BuiltBloom bloom = Builder.bloom().size(new SizeState(width, height)).color(new QuadColorState(color))
                .radius(radius).smoothness(smoothness).blurRadius(blurRadius).build();

        bloom.render(drawContext.getMatrices().peek().getPositionMatrix(), x, y);
    }

    public static void drawRectangle(DrawContext drawContext, float x, float y, float width, float height, double radius1, double radius2, double radius3, double radius4, int color) {
        BuiltRectangle rectangle = Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(color))
                .radius(new QuadRadiusState(radius1, radius2, radius3, radius4)).smoothness(1.0f).build();

        rectangle.render(drawContext.getMatrices().peek().getPositionMatrix(), x, y);
    }

    public static void drawGradient(DrawContext drawContext, float x, float y, float width, float height, double radius1, double radius2, double radius3, double radius4, int color1, int color2) {
        BuiltGradient gradient = Builder.gradient().size(new SizeState(width, height)).color1(color1).color2(color2)
                .radius(new QuadRadiusState(radius1, radius2, radius3, radius4)).smoothness(1.0f).build();

        gradient.render(drawContext.getMatrices().peek().getPositionMatrix(), x, y);
    }

    public static void drawTexture(DrawContext drawContext, Identifier location, float x, float y, float width, float height, float u, float v, float texWidth, float texHeight, double radius1, double radius2, double radius3, double radius4, int color) {
        AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager()
                .getTexture(location);

        BuiltTexture texture = Builder.texture().size(new SizeState(width, height)).radius(new QuadRadiusState(radius1, radius2, radius3, radius4))
                .texture(u, v, texWidth, texHeight, abstractTexture).color(new QuadColorState(color)).build();

        texture.render(drawContext.getMatrices().peek().getPositionMatrix(), x, y);
    }

    public static void drawScissor(float x, float y, float width, float height, Runnable runnable) {
        Window window = MinecraftClient.getInstance().getWindow();
        double scaleFactor = window.getScaleFactor();

        int scissorX = (int) (x * scaleFactor);
        int scissorY = (int) (window.getHeight() - (y + height) * scaleFactor);
        int scissorWidth = (int) (width * scaleFactor);
        int scissorHeight = (int) (height * scaleFactor);

        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
        runnable.run();
        RenderSystem.disableScissor();
    }

}
