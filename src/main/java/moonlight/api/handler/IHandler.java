package moonlight.api.handler;

public interface IHandler {
    void mouseClicked(double mouseX, double mouseY, int mouseButton);
    void mouseReleased(double mouseX, double mouseY, int mouseButton);
    void mouseMoved(double mouseX, double mouseY);
    void keyPressed(int keyCode, int scanCode, int modifiers);
}
