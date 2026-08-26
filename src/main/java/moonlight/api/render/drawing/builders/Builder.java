package moonlight.api.render.drawing.builders;

import moonlight.api.render.drawing.builders.impl.*;

public final class Builder {

    private static final RectangleBuilder RECTANGLE_BUILDER = new RectangleBuilder();
    private static final GradientBuilder GRADIENT_BUILDER = new GradientBuilder();
    private static final BloomBuilder BLOOM_BUILDER = new BloomBuilder();
    private static final BorderBuilder BORDER_BUILDER = new BorderBuilder();
    private static final TextureBuilder TEXTURE_BUILDER = new TextureBuilder();
    private static final TextBuilder TEXT_BUILDER = new TextBuilder();
    private static final BlurBuilder BLUR_BUILDER = new BlurBuilder();

    public static RectangleBuilder rectangle() {
        return RECTANGLE_BUILDER;
    }

    public static GradientBuilder gradient() {
        return GRADIENT_BUILDER;
    }

    public static BloomBuilder bloom() {
        return BLOOM_BUILDER;
    }

    public static BorderBuilder border() {
        return BORDER_BUILDER;
    }

    public static TextureBuilder texture() {
        return TEXTURE_BUILDER;
    }

    public static TextBuilder text() {
        return TEXT_BUILDER;
    }

    public static BlurBuilder blur() {
        return BLUR_BUILDER;
    }

}