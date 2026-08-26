package moonlight.client.gui.widget.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import moonlight.api.handler.other.MouseSystem;
import moonlight.api.math.MathSystem;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.client.gui.widget.Widget;

public class SliderWidget extends Widget {

    private double mouseX;
    private float x1, lastX;

    public float currentValue;
    public float min, max;

    private float progress;

    private boolean isDrag, isFocus;

    private float time;
    private float active;

    public SliderWidget(float min, float max) {
        super("Slider");

        this.min = min;
        this.max = max;
    }

    public SliderWidget(float min, float max, float progress) {
        super("Slider");

        this.min = min;
        this.max = max;
        this.progress = progress;
    }

    @Override
    public void render(DrawContext drawContext, float x, float y, float width, float height, float deltaTime) {
        super.render(drawContext, x, y, width, height, deltaTime);

        if(this.isFocus && !this.isDrag) {
            this.lastX = (float) (this.x1 - this.mouseX);
            this.isDrag = true;

            this.time += deltaTime / 20;
            float t = MathHelper.clamp(this.time / 5, 0.0f, 1.0f);
            float progress = (float) MathSystem.easeIn(t);
            this.active = MathSystem.lerp(0, this.x1, progress);
        }

        DrawSystem.drawRectangle(drawContext, x, y, width, height - 2, 1, 1, 1, 1, GUI_COLOR_6);
        DrawSystem.drawGradient(drawContext, x, y, this.x1 + this.active + 5, height - 2, 1, 1, 1, 1, GUI_COLOR_4, GUI_COLOR_3);
        DrawSystem.drawRectangle(drawContext, x + this.x1 + this.active, y - 3, 11, 11, 5, 5, 5, 5, GUI_COLOR_3);
        TextSystem.drawText(drawContext, String.valueOf((int) this.currentValue), (x + width) + 10, y - 2.5f, 8, -1);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(MouseSystem.isMouseOver(mouseX, mouseY, this.x + this.x1, this.y, 10, 10) && mouseButton == 0)
            this.isFocus = true;

    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.isDrag = false;
        this.isFocus = false;
        this.time = 0;
        this.active = 0;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.mouseX = mouseX;

        if(this.isDrag && this.isFocus) {
            float relative = (float) ((mouseX - this.x) / this.width);

            this.currentValue = MathHelper.clamp(relative * (this.max - this.min) + this.min, this.min, this.max);
            this.x1 = (this.currentValue - this.min) / (this.max - this.min) * this.width - 5;
        }
    }

}
