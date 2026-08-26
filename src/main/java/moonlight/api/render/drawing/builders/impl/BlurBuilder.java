package moonlight.api.render.drawing.builders.impl;

import moonlight.api.render.drawing.builders.AbstractBuilder;
import moonlight.api.render.drawing.builders.states.QuadColorState;
import moonlight.api.render.drawing.builders.states.QuadRadiusState;
import moonlight.api.render.drawing.builders.states.SizeState;
import moonlight.api.render.drawing.renderers.impl.BuiltBlur;

public final class BlurBuilder extends AbstractBuilder<BuiltBlur> {

    private SizeState size;
    private QuadRadiusState radius;
    private QuadColorState color;
    private float smoothness;
    private float blurRadius;

    public BlurBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public BlurBuilder radius(QuadRadiusState radius) {
        this.radius = radius;
        return this;
    }

    public BlurBuilder color(QuadColorState color) {
        this.color = color;
        return this;
    }

    public BlurBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public BlurBuilder blurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
        return this;
    }

    @Override
    protected BuiltBlur _build() {
        return new BuiltBlur(
            this.size,
            this.radius,
            this.color,
            this.smoothness,
            this.blurRadius
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = QuadRadiusState.NO_ROUND;
        this.color = QuadColorState.WHITE;
        this.smoothness = 1.0f;
        this.blurRadius = 0.0f;
    }

}