package moonlight.api.render.drawing.builders.impl;

import moonlight.api.render.drawing.builders.AbstractBuilder;
import moonlight.api.render.drawing.builders.states.QuadColorState;
import moonlight.api.render.drawing.builders.states.SizeState;
import moonlight.api.render.drawing.renderers.impl.BuiltBloom;

public final class BloomBuilder extends AbstractBuilder<BuiltBloom> {

    private SizeState size;
    private QuadColorState color;
    private float radius;
    private float smoothness;
    private float blurRadius;

    public BloomBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public BloomBuilder color(QuadColorState color) {
        this.color = color;
        return this;
    }

    public BloomBuilder radius(float radius) {
        this.radius = radius;
        return this;
    }

    public BloomBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }


    public BloomBuilder blurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
        return this;
    }

    @Override
    protected BuiltBloom _build() {
        return new BuiltBloom(
                this.size,
                this.color,
                this.radius,
                this.smoothness,
                this.blurRadius
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.color = QuadColorState.WHITE;
        this.radius = 1.0f;
        this.smoothness = 1.0f;
        this.blurRadius = 0.0f;
    }

}
