package moonlight.api.render.drawing;

import net.minecraft.client.gui.DrawContext;

import moonlight.Moonlight;
import moonlight.api.render.drawing.builders.Builder;
import moonlight.api.render.drawing.renderers.impl.BuiltText;

public class TextSystem {

    public static void drawText(DrawContext drawContext, String input, float x, float y, float size, int color) {
        if(input == null || input.isEmpty())
            return;

        BuiltText text = Builder.text().font(Moonlight.INTER_FONT.get()).text(input)
                .color(color).size(size).thickness(0.05f).build();

        try {
            text.render(drawContext.getMatrices().peek().getPositionMatrix(), x, y);
        } catch (IllegalStateException ignored) {
        }
    }

    public static void drawTextBorder(DrawContext drawContext, String input, float x, float y, float size, int max, int color) {
        String cut = input.substring(0, Math.min(input.length(), max)).concat("...");

        float widthCut = Moonlight.INTER_FONT.get().getWidth(cut, size);

        if(max > 0 && max < input.length() + 1) {
            DrawSystem.drawScissor(x, y, widthCut, size, () ->
                    drawText(drawContext, cut, x, y, size, color));
        } else {
            drawText(drawContext, input, x, y, size, color);
        }

    }

}
