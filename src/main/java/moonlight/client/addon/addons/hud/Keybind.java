package moonlight.client.addon.addons.hud;

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

import java.awt.Color;
import java.util.List;

public class Keybind extends Addon implements IStyle {

    public float x = 100, y = 50;
    public float width = 130, height = 26;

    private float contentHeight;

    public Keybind() {
        super("Hot Keys", GLFW.GLFW_KEY_N, Type.UTILS);
        Draggable.draggables.add(new Draggable(this.getName(), this.x, this.y, this.width, this.height));
    }

    private Draggable drag() {
        for(Draggable d : Draggable.draggables)
            if(d.name.equals(this.getName())) return d;

        return Draggable.draggables.getFirst();
    }

    @EventSubscribe
    public void eventRender2D(EventRender2D event) {
        Draggable d = drag();

        float x = d.x, y = d.y;
        var graphics = event.getGraphics();
        var font = Moonlight.INTER_FONT.get();

        List<Addon> bound = Moonlight.addonSystem.getModules().stream()
                .filter(addon -> addon.getKey() != -1)
                .toList();

        this.contentHeight = bound.size() * 15;

        float height = this.height + this.contentHeight;

        DrawSystem.drawBlur(graphics, x, y, this.width, height, 8, 8, 8, 8,
                new Color(16, 16, 26, 225).getRGB());
        DrawSystem.drawBorder(graphics, x, y, this.width, height, 8, 8, 8, 8,
                new Color(48, 48, 72).getRGB());

        //header
        DrawSystem.drawRectangle(graphics, x + 8, y + 7, 12, 12, 4, 4, 4, 4, new Color(138, 99, 255).getRGB());
        TextSystem.drawText(graphics, this.getName(), x + 26, y + 8, 11, -1);

        //rows
        float offset = 0;

        for(Addon addon : bound) {
            float rowY = y + this.height + offset;
            String keyName = keyLabel(addon.getKey());

            TextSystem.drawText(graphics, addon.getName(), x + 8, rowY + 2, 9, new Color(165, 165, 185).getRGB());
            TextSystem.drawText(graphics, keyName, x + this.width - 8 - font.getWidth(keyName, 9), rowY + 2, 9, -1);

            offset += 15;
        }
    }

    private static String keyLabel(int key) {
        if(key == GLFW.GLFW_KEY_RIGHT_SHIFT) return "RShift";
        if(key == GLFW.GLFW_KEY_LEFT_SHIFT) return "LShift";

        String name = GLFW.glfwGetKeyName(key, GLFW.glfwGetKeyScancode(key));

        if(name == null)
            return String.valueOf(key);

        String clean = name.replaceAll("[^a-zA-Z0-9]", "");
        return clean.isEmpty() ? String.valueOf(key) : clean.toUpperCase();
    }

}
