package moonlight.api.event.events;

import moonlight.api.event.Event;
import org.joml.Matrix4f;

public class EventRender3D extends Event {

    private final Matrix4f poseStack;

    public EventRender3D(Matrix4f poseStack) {
        this.poseStack = poseStack;
    }

    public Matrix4f getPoseStack() {
        return poseStack;
    }

}
