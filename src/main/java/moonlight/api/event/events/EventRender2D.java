package moonlight.api.event.events;

import moonlight.api.event.Event;
import net.minecraft.client.gui.DrawContext;

public class EventRender2D extends Event {

    private final DrawContext graphics;
    private final float deltaTime;

    public EventRender2D(DrawContext graphics, float deltaTime) {
        this.graphics = graphics;
        this.deltaTime = deltaTime;
    }

    public DrawContext getGraphics() {
        return graphics;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

}
