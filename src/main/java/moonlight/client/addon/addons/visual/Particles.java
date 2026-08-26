package moonlight.client.addon.addons.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticlesMode;

import moonlight.client.addon.Addon;
import moonlight.client.addon.Type;

public class Particles extends Addon {

    private ParticlesMode previous = ParticlesMode.ALL;

    public Particles() {
        super("Particles", Type.VISUAL);
    }

    @Override
    public void enable() {
        this.previous = MinecraftClient.getInstance().options.getParticles().getValue();
        MinecraftClient.getInstance().options.getParticles().setValue(ParticlesMode.MINIMAL);
        super.enable();
    }

    @Override
    public void disable() {
        MinecraftClient.getInstance().options.getParticles().setValue(this.previous);
        super.disable();
    }

}
