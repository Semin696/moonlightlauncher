package moonlight.api.render.particle;

import net.minecraft.client.gui.DrawContext;

import net.minecraft.util.math.MathHelper;

import moonlight.api.render.IRender;
import moonlight.api.render.drawing.TextSystem;
import moonlight.api.timer.timers.SimpleTimer;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class ParticleSystem implements IRender {

    private final SimpleTimer simpleTimer = new SimpleTimer();
    private final LinkedList<Particle> count = new LinkedList<>();

    private boolean isTimer;

    int max, delay;

    public ParticleSystem(int max, int delay) {
        this.max = max;
        this.delay = delay;
    }

    @Override
    public void render(DrawContext drawContext, float x, float y, float width, float height, float deltaTime) {
        Random random = new Random();

        if(!this.isTimer) {
            this.simpleTimer.setDuration(random.nextLong(this.delay / 3, this.delay)).run();
            this.isTimer = true;
        }

        if(this.simpleTimer.hasFinished() && (this.count.size() < this.max)) {
            final float x1 = random.nextFloat(x, width);
            final float y1 = random.nextFloat(y, height);

            this.count.add(new Particle(x1, y1, 255));
            this.isTimer = false;
        }

        if(!this.count.isEmpty()) {
            for(Particle particle : this.count) {
                particle.alpha -= (int) 1.5f;

                if(particle.alpha == 0) {
                    this.count.remove(particle);
                    break;
                }

                TextSystem.drawText(drawContext, "$", particle.x, particle.y, 5, new Color(255, 255, 255, MathHelper.clamp(particle.alpha, 0, 255)).getRGB());
                //DrawSystem.drawRectangle(drawContext, particle.x, particle.y, 5, 5, 7, 7, 7, 7, new Color(1, 1, 1, MathHelper.clamp(particle.alpha, 0, 255)).getRGB());
            }
        }
    }

}
