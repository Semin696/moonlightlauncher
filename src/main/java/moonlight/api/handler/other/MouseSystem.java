package moonlight.api.handler.other;

public class MouseSystem {

    public static boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height) {
        return (mouseX >= x && mouseX <= x + width) && (mouseY >= y && mouseY <= y + height);
    }

}
