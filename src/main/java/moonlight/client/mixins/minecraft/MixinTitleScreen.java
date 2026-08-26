package moonlight.client.mixins.minecraft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import moonlight.Moonlight;
import moonlight.client.account.AccountManager;
import moonlight.client.gui.screens.mainMenu.MainMenu;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void init(CallbackInfo ci) {
        AccountManager.get().load();
        Moonlight.LOGGER.info("Main menu: replaced title screen");

        MinecraftClient.getInstance().setScreen(new MainMenu());
        ci.cancel();
    }

}
