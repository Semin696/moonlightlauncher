package moonlight.client.addon.addons.hud;

import net.fabricmc.loader.impl.util.StringUtil;

import org.lwjgl.glfw.GLFW;

import moonlight.Moonlight;
import moonlight.api.event.EventSubscribe;
import moonlight.api.event.events.EventRender2D;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.client.addon.Addon;
import moonlight.client.addon.Type;
import moonlight.client.gui.IStyle;
import moonlight.client.gui.screens.draggableGui.comp.Draggable;
import moonlight.client.gui.widget.IWidget;
import moonlight.client.gui.widget.widgets.CheckBoxWidget;
import moonlight.client.gui.widget.widgets.SliderWidget;

import java.awt.*;
import java.util.StringJoiner;

public class Logotype extends Addon implements IStyle {

    @IWidget
    public final SliderWidget sliderWidget = new SliderWidget(0, 10);
    @IWidget
    public final CheckBoxWidget checkBoxWidget = new CheckBoxWidget("test1");
    @IWidget
    public final CheckBoxWidget checkBoxWidget1 = new CheckBoxWidget("idinaxyidayn");
    @IWidget
    public final CheckBoxWidget checkBoxWidget2 = new CheckBoxWidget("samidinaxyu");
    @IWidget
    public final CheckBoxWidget checkBoxWidget3 = new CheckBoxWidget("vi 2 dolbaeba");

    public float x = 10, y = 10;
    public float width = 0, height = 18;

    public Logotype() {
        super("Logotype", GLFW.GLFW_KEY_M, Type.HUD);
        super.addWidget(this.sliderWidget, this.checkBoxWidget, this.checkBoxWidget1, this.checkBoxWidget2, this.checkBoxWidget3);
        Draggable.draggables.add(new Draggable(this.getName(), this.x, this.y, this.width, this.height));
    }

    @EventSubscribe
    public void eventRender2D(EventRender2D eventRender2D) {
        StringJoiner message = new StringJoiner(" ")
                .add(StringUtil.capitalize(Moonlight.MOD_ID))
                .add(Moonlight.session.toString());

        this.width = Moonlight.INTER_FONT.get().getWidth(message.toString(), 10) + 12;

        DrawSystem.drawBlur(eventRender2D.getGraphics(), this.x, this.y, this.width, this.height, 3, 3, 3, 3, new Color(23, 23, 23).getRGB());
        TextSystem.drawText(eventRender2D.getGraphics(), message.toString(), this.x + 4, this.y + 2.5f, 10, -1);
    }

}
