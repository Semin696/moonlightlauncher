package moonlight.client.gui.screens.draggableGui.comp;

import java.util.ArrayList;
import java.util.List;

public class Draggable {

    public static final List<Draggable> draggables = new ArrayList<>();

    public String name;

    public boolean isFocus, isDrag;

    public float lastX, lastY;
    public float x, y, width, height;

    public float velocityX, velocityY;
    public float releaseX, releaseY;

    public Draggable(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void update(float dt) {
        if(this.isDrag) return;

        float stiffness = 42f;
        float damping = 0.86f;

        this.velocityX += (this.releaseX - this.x) * stiffness * dt;
        this.velocityY += (this.releaseY - this.y) * stiffness * dt;

        this.velocityX *= damping;
        this.velocityY *= damping;

        if(Math.abs(this.velocityX) < 4f && Math.abs(this.velocityY) < 4f
                && Math.abs(this.releaseX - this.x) < 0.4f && Math.abs(this.releaseY - this.y) < 0.4f) {
            this.x = this.releaseX;
            this.y = this.releaseY;
            this.velocityX = 0;
            this.velocityY = 0;
            return;
        }

        this.x += this.velocityX * dt;
        this.y += this.velocityY * dt;
    }

}
