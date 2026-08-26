package moonlight.client.mixins.minecraft;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "updateWindowTitle", at = @At("HEAD"), cancellable = true)
    private void updateWindowTitle(CallbackInfo ci) {
        MinecraftClient.getInstance().getWindow().setTitle("Moonlight Visual");
        ci.cancel();
    }

}
