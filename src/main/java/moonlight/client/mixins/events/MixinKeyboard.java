package moonlight.client.mixins.events;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import moonlight.api.event.EventSystem;
import moonlight.api.event.events.EventKeyboard;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        EventKeyboard eventKeyboard = new EventKeyboard(key, action);
        EventSystem.post(eventKeyboard);
    }

}
