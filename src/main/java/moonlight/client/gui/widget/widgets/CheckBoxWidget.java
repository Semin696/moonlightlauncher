package moonlight.client.gui.widget.widgets;

import net.minecraft.client.gui.DrawContext;

import net.minecraft.util.math.MathHelper;
import moonlight.api.handler.other.MouseSystem;
import moonlight.api.math.MathSystem;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.client.gui.widget.Widget;

import java.awt.*;

public class CheckBoxWidget extends Widget {

    public boolean isActive;
    protected float width = 26, height = 13.5f;

    private float time;
    private float active;

    public CheckBoxWidget(String name) {
        super(name);
    }

    @Override
    public void render(DrawContext drawContext, float x, float y, float deltaTime) {
        super.render(drawContext, x, y, deltaTime);

        if(this.isActive) {
            this.time += deltaTime / 20;
            float t = MathHelper.clamp(this.time * 4, 0.0f, 1.0f);
            float progress = (float) MathSystem.easeBoth(t);
            this.active = MathSystem.lerp(2.0f, 14.0f, progress);
        } else {
            this.time = 0;
            this.active = 2;
        }

        DrawSystem.drawRectangle(drawContext, x, y, this.width, this.height, 5, 5, 5, 5, GUI_COLOR_6);
        DrawSystem.drawRectangle(drawContext, x + this.active, y + 1.7f, 10, 10, 4, 4, 4, 4,
                this.isActive ? Color.GRAY.getRGB() : GUI_COLOR_3);
        TextSystem.drawText(drawContext, this.name, (x + width) + 10, y + 1, 8, this.isActive ? -1 : Color.GRAY.getRGB());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(MouseSystem.isMouseOver(mouseX, mouseY, this.x, this.y, this.width, this.height) && mouseButton == 0)
            this.isActive = !this.isActive;
    }

}
