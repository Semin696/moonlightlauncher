package moonlight.client.handler;

import net.minecraft.client.MinecraftClient;

import org.lwjgl.glfw.GLFW;

import moonlight.api.event.EventSubscribe;
import moonlight.api.event.events.EventMouse;
import moonlight.api.event.events.EventRender2D;
import moonlight.api.handler.other.MouseSystem;
import moonlight.client.gui.screens.draggableGui.comp.Draggable;

public class DragHandler {

    private Draggable active;
    private float offX, offY;
    private float prevX, prevY;

    @EventSubscribe
    public void onMouse(EventMouse event) {
        if(event.getButton() != 0) return;

        if(event.getAction() == GLFW.GLFW_PRESS) {
            for(Draggable draggable : Draggable.draggables) {
                if(MouseSystem.isMouseOver(event.getMouseX(), event.getMouseY(),
                        draggable.x, draggable.y, draggable.width, draggable.height)) {
                    this.active = draggable;
                    this.offX = (float) (event.getMouseX() - draggable.x);
                    this.offY = (float) (event.getMouseY() - draggable.y);
                    this.prevX = draggable.x;
                    this.prevY = draggable.y;
                    draggable.velocityX = 0;
                    draggable.velocityY = 0;
                    break;
                }
            }
        } else if(event.getAction() == GLFW.GLFW_RELEASE && this.active != null) {
            this.active.releaseX = this.active.x;
            this.active.releaseY = this.active.y;
            this.active = null;
        }
    }

    @EventSubscribe
    public void onRender(EventRender2D event) {
        if(this.active == null) return;

        var mouse = MinecraftClient.getInstance().mouse;
        float nx = (float) mouse.getX() - this.offX;
        float ny = (float) mouse.getY() - this.offY;

        float dt = Math.max(event.getDeltaTime(), 0.001f);
        this.active.velocityX = (nx - this.prevX) / dt;
        this.active.velocityY = (ny - this.prevY) / dt;

        this.active.x = nx;
        this.active.y = ny;
        this.prevX = nx;
        this.prevY = ny;
    }

}
