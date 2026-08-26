package moonlight.client.mixins.events;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import moonlight.api.event.EventSystem;
import moonlight.api.event.events.EventRender2D;

@Mixin(InGameHud.class)
public class MixinRender2D {

    @Inject(method = "render", at = @At("RETURN"))
    public void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        EventRender2D eventRender2D = new EventRender2D(context, tickCounter.getLastFrameDuration());
        EventSystem.post(eventRender2D);
    }

}
