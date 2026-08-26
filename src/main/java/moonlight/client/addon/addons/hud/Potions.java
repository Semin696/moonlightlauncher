package moonlight.client.addon.addons.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import moonlight.Moonlight;
import moonlight.api.event.EventSubscribe;
import moonlight.api.event.events.EventRender2D;
import moonlight.api.render.drawing.DrawSystem;
import moonlight.api.render.drawing.TextSystem;
import moonlight.api.timer.TimerSystem;
import moonlight.client.addon.Addon;
import moonlight.client.addon.Type;
import moonlight.client.gui.IStyle;
import moonlight.client.gui.screens.draggableGui.comp.Draggable;

import java.awt.*;
import java.util.Map;

public class Potions extends Addon implements IStyle {

    float x = 200, y = 200;
    float width = 120, height = 18;

    private float heightOffset;

    public Potions() {
        super("Potions", GLFW.GLFW_KEY_N, Type.HUD);
        Draggable.draggables.add(new Draggable(this.getName(), this.x, this.y, this.width, this.height + this.heightOffset + 5));
    }

    @EventSubscribe
    public void eventRender2D(EventRender2D eventRender2D) {
        float offset = 0;

        float dragX = Draggable.draggables.get(2).x;
        float dragY = Draggable.draggables.get(2).y;

        DrawSystem.drawGradient(eventRender2D.getGraphics(), dragX, dragY, width, height, 3, 0, 0, 3, GUI_COLOR_4, GUI_COLOR_3);
        DrawSystem.drawBlur(eventRender2D.getGraphics(), dragX, dragY + height - 1.3f, width, this.heightOffset + 5, 0, 3, 3, 0, new Color(23, 23, 23).getRGB());
        TextSystem.drawText(eventRender2D.getGraphics(), this.getName(), dragX + 4, dragY + 3, 10, -1);

        if(MinecraftClient.getInstance().player != null) {
            for (Map.Entry<RegistryEntry<StatusEffect>, StatusEffectInstance> effect : MinecraftClient.getInstance().player.getActiveStatusEffects().entrySet()) {
                String name = String.format("%s", effect.getValue().getTranslationKey().replaceAll("effect.minecraft.", ""));
                String time = String.format("%d:%d", TimerSystem.minute(effect.getValue().getDuration()), TimerSystem.second(effect.getValue().getDuration()));

                StatusEffect statusEffect = effect.getKey().value();
                Identifier effectId = Registries.STATUS_EFFECT.getId(statusEffect);
                Identifier icon = Identifier.of("minecraft", "textures/mob_effect/" + effectId.getPath() + ".png");

                DrawSystem.drawTexture(eventRender2D.getGraphics(), icon, dragX + 5, dragY + this.heightOffset + 5.5f, 12, 12, 1, 1, 1, 1, 2, 2, 2, 2, -1);
                TextSystem.drawTextBorder(eventRender2D.getGraphics(), name, dragX + 20, dragY + this.heightOffset + 5.5f, 9, 12, Color.LIGHT_GRAY.getRGB());
                TextSystem.drawTextBorder(eventRender2D.getGraphics(), time, dragX + this.width - (Moonlight.INTER_FONT.get().getWidth(time, 9) + 5), dragY + this.heightOffset + 5.5f, 9, 6, Color.LIGHT_GRAY.getRGB());

                offset += 15;
                this.heightOffset = offset;
            }
        }
    }
}
