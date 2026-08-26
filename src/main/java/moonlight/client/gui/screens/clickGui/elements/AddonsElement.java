package moonlight.client.gui.screens.clickGui.elements;

import net.minecraft.client.gui.DrawContext;

import moonlight.Moonlight;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.client.addon.Addon;
import moonlight.client.gui.IGui;
import moonlight.client.gui.IStyle;
import moonlight.client.gui.screens.clickGui.ClickGui;
import moonlight.client.gui.widget.Widget;
import moonlight.client.gui.widget.widgets.CheckBoxWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AddonsElement implements IGui, IStyle {

    private final List<Widget> checkbox = new ArrayList<>();
    private final List<Addon> addons = Moonlight.addonSystem.getModules();

    @Override
    public void render(DrawContext drawContext, float x, float y, float deltaTime) {
        int column = 2;

        for(int i = 0; i < Moonlight.addonSystem.getModulesByType(ClickGui.currentType).size(); i++) {
            Addon addon = Moonlight.addonSystem.getModulesByType(ClickGui.currentType).get(i);

            this.checkbox.add(new CheckBoxWidget(" "));
            ((CheckBoxWidget) this.checkbox.get(i)).isActive = addon.isEnable();

            int offsetY = 0;

            int width = 145;
            int height = 20;

            int row = i / column;
            int col = i % column;

            int x1 = (int) (x + col * (width + 20));
            int y1 = (int) (y + row * (height + 5));

            int prev = i - column;
            if(prev >= 0 && prev < this.addons.size()) y1 += (int) this.addons.get(prev).offset;

            DrawSystem.drawBlur(drawContext, x1 + 21, y1 + 45.5f, width, height + addon.offset - 1.5f, 3, 3, 3, 3, new Color(23, 23, 23, 155).getRGB());
            TextSystem.drawText(drawContext, addon.getName(), x1 + 28, y1 + 52.5f, 11, addon.isEnable() ? Color.WHITE.getRGB() : Color.WHITE.darker().getRGB());
            this.checkbox.get(i).render(drawContext, (x1 + width) - 10.5f, y1 + 52.5f, deltaTime);

            for(Widget widget : addon.widgets) {
                widget.render(drawContext, x1 + 30, y1 + 58 + height + offsetY, deltaTime);
                widget.render(drawContext, x1 + 30, y1 + 58 + height + offsetY, 80, 7, deltaTime);

                offsetY += 23;
            }

            addon.offset = offsetY + 10;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for(int i = 0; i < Moonlight.addonSystem.getModulesByType(ClickGui.currentType).size(); i++) {
            Addon addon = Moonlight.addonSystem.getModulesByType(ClickGui.currentType).get(i);

            if(addon.getModuleType().equals(ClickGui.currentType)) {
                CheckBoxWidget checkbox = (CheckBoxWidget) this.checkbox.get(i);
                boolean wasActiveBefore = checkbox.isActive;

                checkbox.mouseClicked(mouseX, mouseY, mouseButton);

                if (wasActiveBefore != checkbox.isActive)
                    addon.toggleModule(addon);

                for (Widget widget : addon.widgets)
                    widget.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        for(int i = 0; i < Moonlight.addonSystem.getModulesByType(ClickGui.currentType).size(); i++)
            this.checkbox.get(i).mouseReleased(mouseX, mouseY, mouseButton);

        for (Addon addon : addons) {
            if (addon.getModuleType().equals(ClickGui.currentType)) {
                for (Widget widget : addon.widgets)
                    widget.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (Addon addon : Moonlight.addonSystem.getModulesByType(ClickGui.currentType)) {
            if (addon.getModuleType().equals(ClickGui.currentType)) {
                for (Widget widget : addon.widgets)
                    widget.mouseMoved(mouseX, mouseY);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

}
