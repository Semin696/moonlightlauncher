package moonlight.api.render.drawing.builders.impl;

import moonlight.api.render.drawing.builders.AbstractBuilder;
import moonlight.api.render.drawing.builders.states.QuadColorState;
import moonlight.api.render.drawing.builders.states.QuadRadiusState;
import moonlight.api.render.drawing.builders.states.SizeState;
import moonlight.api.render.drawing.renderers.impl.BuiltGradient;

import java.awt.*;

public class GradientBuilder extends AbstractBuilder<BuiltGradient> {

    private SizeState size;
    private QuadRadiusState radius;
    private int color1;
    private int color2;
    private float smoothness;

    public GradientBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public GradientBuilder radius(QuadRadiusState radius) {
        this.radius = radius;
        return this;
    }

    public GradientBuilder color1(int color) {
        this.color1 = color;
        return this;
    }

    public GradientBuilder color2(int color) {
        this.color2 = color;
        return this;
    }

    public GradientBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    @Override
    protected BuiltGradient _build() {
        return new BuiltGradient(
                this.size,
                this.radius,
                this.color1,
                this.color2,
                this.smoothness
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = QuadRadiusState.NO_ROUND;
        this.color1 = Color.WHITE.getRGB();
        this.color2 = Color.WHITE.getRGB();
        this.smoothness = 1.0f;
    }
}
