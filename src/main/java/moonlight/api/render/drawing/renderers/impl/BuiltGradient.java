package moonlight.api.render.drawing.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.*;

import org.joml.Matrix4f;

import moonlight.api.render.drawing.builders.states.QuadRadiusState;
import moonlight.api.render.drawing.builders.states.SizeState;
import moonlight.api.render.drawing.renderers.IRenderer;
import moonlight.api.render.providers.ResourceProvider;

public record BuiltGradient(
        SizeState size,
        QuadRadiusState radius,
        int color1,
        int color2,
        float smoothness
) implements IRenderer {

    private static final ShaderProgramKey GRADIENT_SHADER_KEY = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("rectangle"),
            VertexFormats.POSITION_COLOR, Defines.EMPTY);

    @Override
    public void render(Matrix4f matrix, float x, float y, float z) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        float width = this.size.width(), height = this.size.height();

        ShaderProgram shader = RenderSystem.setShader(GRADIENT_SHADER_KEY);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(this.radius.radius1(), this.radius.radius2(),
                this.radius.radius3(), this.radius.radius4());
        shader.getUniform("Smoothness").set(this.smoothness);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix, x, y, z).color(this.color1);
        builder.vertex(matrix, x, y + height, z).color(this.color1);
        builder.vertex(matrix, x + width, y + height, z).color(this.color2);
        builder.vertex(matrix, x + width, y, z).color(this.color2);

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

}
