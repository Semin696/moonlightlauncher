package moonlight.client.gui.screens.draggableGui;

import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import moonlight.api.handler.other.MouseSystem;
import moonlight.client.gui.screens.draggableGui.comp.Draggable;

public class DraggableGui extends Screen {

    private float blur;
    private long lastFrame;

    public DraggableGui() {
        super(Text.of(StringUtil.capitalize("DraggableGui")));
    }

    @Override
    public void init() {
        assert this.client != null;
        this.blur = this.client.options.getMenuBackgroundBlurrinessValue();
        this.client.options.getMenuBackgroundBlurriness().setValue(0);

        this.lastFrame = Util.getMeasuringTimeNano();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = Util.getMeasuringTimeNano();
        float dt = Math.min((now - this.lastFrame) / 1_000_000_000f, 0.05f);
        this.lastFrame = now;

        for(Draggable draggable : Draggable.draggables) {
            if(draggable.isFocus && !draggable.isDrag) {
                draggable.lastX = draggable.x - mouseX;
                draggable.lastY = draggable.y - mouseY;
                draggable.isDrag = true;
                draggable.velocityX = 0;
                draggable.velocityY = 0;
            }

            draggable.update(dt);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for(Draggable draggable : Draggable.draggables)
            draggable.isFocus = MouseSystem.isMouseOver(mouseX, mouseY, draggable.x, draggable.y, draggable.width, draggable.height) && button == 0;

        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for(Draggable draggable : Draggable.draggables) {
            if(draggable.isDrag && draggable.isFocus) {
                float newX = (float) (mouseX + draggable.lastX);
                float newY = (float) (mouseY + draggable.lastY);

                draggable.velocityX = (newX - draggable.x) * 60f;
                draggable.velocityY = (newY - draggable.y) * 60f;

                draggable.x = newX;
                draggable.y = newY;
            }
        }

        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(Draggable draggable : Draggable.draggables) {
            if(draggable.isDrag) {
                draggable.releaseX = draggable.x;
                draggable.releaseY = draggable.y;
            }

            draggable.isDrag = false;
            draggable.isFocus = false;
        }

        return true;
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.options.getMenuBackgroundBlurriness().setValue((int) this.blur);
        super.close();
    }

}
