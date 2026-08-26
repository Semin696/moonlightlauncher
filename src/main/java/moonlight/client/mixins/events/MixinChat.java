package moonlight.client.mixins.events;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import moonlight.api.event.EventSystem;
import moonlight.api.event.events.EventChat;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinChat {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessage(String content, CallbackInfo ci) {
        EventChat event = EventSystem.post(new EventChat(content));

        if(event.isCanceled())
            ci.cancel();
    }

}
