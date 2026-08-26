package moonlight.client.gui.screens.clickGui.elements;

import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

import moonlight.Moonlight;
import moonlight.api.handler.other.MouseSystem;
import moonlight.api.render.IRender;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.api.render.particle.ParticleSystem;
import moonlight.client.addon.Type;
import moonlight.client.gui.IGui;
import moonlight.client.gui.IStyle;
import moonlight.client.gui.screens.clickGui.ClickGui;
import moonlight.client.gui.screens.draggableGui.DraggableGui;

import java.awt.*;

public class PanelElement implements IGui, IStyle {

    private final IRender particle = new ParticleSystem(20, 1000);

    private float x, y, width, height;

    @Override
    public void render(DrawContext drawContext, float x, float y, float width, float height, float deltaTime) {
        int offsetX = 0;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if(ClickGui.currentType.equals(Type.DRAG))
            MinecraftClient.getInstance().setScreen(new DraggableGui());

        //title
        DrawSystem.drawGradient(drawContext, x, y, width, 33,
                6, 0, 0, 6, GUI_COLOR_4, GUI_COLOR_3);
        TextSystem.drawText(drawContext, StringUtil.capitalize(Moonlight.MOD_ID), x + 15, y + 7.5f, 14, -1);

        this.particle.render(drawContext, x + 15, y + 3, Moonlight.INTER_FONT.get().getWidth(StringUtil.capitalize(Moonlight.MOD_ID), 14) + x, y + 25, deltaTime);

        //button
        for(Type type : Type.values()) {
            int color = ClickGui.currentType.equals(type)
                    ? -1 : Color.WHITE.darker().getRGB();

            TextSystem.drawText(drawContext, type.name().toLowerCase(), (x + 35) +
                    Moonlight.INTER_FONT.get().getWidth(StringUtil.capitalize(Moonlight.MOD_ID), 14) + offsetX, y + 10.5f, 10, color);
            offsetX += 28;
        }

        //profile
        assert MinecraftClient.getInstance().player != null;
        Identifier skinTexture = MinecraftClient.getInstance().getSkinProvider().getSkinTextures(MinecraftClient.getInstance().player.getGameProfile()).texture();

        String playerName = MinecraftClient.getInstance().player.getName().getString();
        String playerPing = "ping: " + ((MinecraftClient.getInstance().player.getServer() != null)
                ? MinecraftClient.getInstance().player.getServer().getPlayerIdleTimeout()
                : "host");

        DrawSystem.drawRectangle(drawContext, (x + width) - 33, y + 6.5f, 20, 20, 9, 9, 9, 9, GUI_COLOR_1);
        DrawSystem.drawTexture(drawContext, skinTexture, (x + width) - 33, y + 6.5f, 20, 20, 0.125f, 0.128f, 0.125f, 0.120f, 9, 9, 9, 9, Color.WHITE.getRGB());
        TextSystem.drawText(drawContext, playerName, (x + width) - (Moonlight.INTER_FONT.get().getWidth(playerName, 9) + 40), y + 7.5f, 9, -1);
        TextSystem.drawText(drawContext, playerPing, (x + width) - (Moonlight.INTER_FONT.get().getWidth(playerPing, 7) + 40), y + 17.5f, 7, Color.LIGHT_GRAY.getRGB());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int offsetX = 0;

        for(Type type : Type.values()) {
            if(MouseSystem.isMouseOver(mouseX, mouseY, (this.x + 35) + Moonlight.INTER_FONT.get().getWidth(StringUtil.capitalize(Moonlight.MOD_ID), 14)
                    + offsetX, this.y + 10.5f, Moonlight.INTER_FONT.get().getWidth(type.name().toLowerCase(), 10), 10) && mouseButton == 0)
                ClickGui.currentType = type;

            offsetX += 28;
        }

        assert MinecraftClient.getInstance().player != null;
        if(MouseSystem.isMouseOver(mouseX, mouseY, (this.x + this.width) - (33 + Moonlight.INTER_FONT.get().getWidth(MinecraftClient.getInstance().player.getName().getString(), 11)),
                this.y + 9.5f, Moonlight.INTER_FONT.get().getWidth(MinecraftClient.getInstance().player.getName().getString(), 11), 15) && mouseButton == 0)
            GLFW.glfwSetClipboardString(MinecraftClient.getInstance().getWindow().getHandle(), MinecraftClient.getInstance().player.getName().getString());
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
