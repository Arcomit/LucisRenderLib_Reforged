package mod.arcomit.lucisrenderlib.client.renderer.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import mod.arcomit.lucisrenderlib.client.util.RenderMatrixHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-08 14:44
 * @Description:  后处理着色器
 */
public class PostShader {
    @Getter protected EffectInstance effect;

    public PostShader(EffectInstance effect) {
        this.effect = effect;
    }

    /**
     * 带参数配置的渲染方法
     * @param shaderLocation shader的目录，lucisrenderlib:example对应目录 assets/lucisrenderlib/shaders/program/example.json
     */
    public PostShader(ResourceLocation shaderLocation) throws IOException {
        this(createEffectInstance(shaderLocation));
    }

    /**
     * 创建 EffectInstance 的辅助方法
     */
    private static EffectInstance createEffectInstance(ResourceLocation shaderLocation) throws IOException {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return new EffectInstance(resourceManager, shaderLocation.toString());
    }

    /**
     * 基础渲染方法
     */
    public void draw() {
        draw(null);
    }

    /**
     * 带参数配置的渲染方法
     * @param parameterSetter 用于设置shader参数的回调函数
     */
    public void draw(Consumer<EffectInstance> parameterSetter) {
        // 设置着色器参数
        RenderTarget previousTarget = Minecraft.getInstance().getMainRenderTarget();
        int prevViewportWidth = GlStateManager. Viewport.width();
        int prevViewportHeight = GlStateManager.Viewport.height();
        this.setMatrix4("ProjMat", RenderMatrixHelper.orthographic(previousTarget));
        this.setVector2("OutSize", (float) prevViewportWidth, (float) prevViewportHeight);
        if (parameterSetter != null) {
            parameterSetter.accept(this.effect);
        }

        this.effect.apply();

        // 缓存深度状态
        boolean prevDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean prevDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        // 禁用深度测试和深度写入
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // 缓存混合状态
        boolean prevBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        int prevBlendSrcRGB = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int prevBlendDstRGB = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int prevBlendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int prevBlendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        // 启用混合并设置混合函数
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(0.0D, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex(prevViewportWidth, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex(prevViewportWidth, prevViewportHeight, 500.0D).endVertex();
        bufferbuilder.vertex(0.0D, prevViewportHeight, 500.0D).endVertex();
        BufferUploader.draw(bufferbuilder.end());

        // 恢复之前缓存的深度状态
        if (prevDepthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.depthMask(prevDepthMask);

        // 恢复之前缓存的混合状态
        if (prevBlend) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }
        RenderSystem.blendFuncSeparate(
                mapBlendFactorToSource(prevBlendSrcRGB),
                mapBlendFactorToDest(prevBlendDstRGB),
                mapBlendFactorToSource(prevBlendSrcAlpha),
                mapBlendFactorToDest(prevBlendDstAlpha)
        );

        this.effect.clear();
    }

    /**
     * 将 OpenGL 混合因子常量映射到 GlStateManager.SourceFactor 枚举
     */
    private GlStateManager.SourceFactor mapBlendFactorToSource(int glFactor) {
        return switch (glFactor) {
            case GL11.GL_ZERO -> GlStateManager.SourceFactor.ZERO;
            case GL11.GL_ONE -> GlStateManager.SourceFactor.ONE;
            case GL11.GL_SRC_COLOR -> GlStateManager.SourceFactor.SRC_COLOR;
            case GL11.GL_ONE_MINUS_SRC_COLOR -> GlStateManager.SourceFactor.ONE_MINUS_SRC_COLOR;
            case GL11.GL_DST_COLOR -> GlStateManager.SourceFactor.DST_COLOR;
            case GL11.GL_ONE_MINUS_DST_COLOR -> GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR;
            case GL11.GL_SRC_ALPHA -> GlStateManager.SourceFactor.SRC_ALPHA;
            case GL11.GL_ONE_MINUS_SRC_ALPHA -> GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA;
            case GL11.GL_DST_ALPHA -> GlStateManager.SourceFactor.DST_ALPHA;
            case GL11.GL_ONE_MINUS_DST_ALPHA -> GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA;
            case GL14.GL_CONSTANT_COLOR -> GlStateManager.SourceFactor.CONSTANT_COLOR;
            case GL14.GL_ONE_MINUS_CONSTANT_COLOR -> GlStateManager.SourceFactor.ONE_MINUS_CONSTANT_COLOR;
            case GL14.GL_CONSTANT_ALPHA -> GlStateManager.SourceFactor.CONSTANT_ALPHA;
            case GL14.GL_ONE_MINUS_CONSTANT_ALPHA -> GlStateManager.SourceFactor.ONE_MINUS_CONSTANT_ALPHA;
            case GL11.GL_SRC_ALPHA_SATURATE -> GlStateManager.SourceFactor.SRC_ALPHA_SATURATE;
            default -> GlStateManager.SourceFactor.ONE;
        };
    }

    /**
     * 将 OpenGL 混合因子常量映射到 GlStateManager.DestFactor 枚举
     */
    private GlStateManager.DestFactor mapBlendFactorToDest(int glFactor) {
        return switch (glFactor) {
            case GL11.GL_ZERO -> GlStateManager.DestFactor.ZERO;
            case GL11.GL_ONE -> GlStateManager.DestFactor.ONE;
            case GL11.GL_SRC_COLOR -> GlStateManager.DestFactor.SRC_COLOR;
            case GL11.GL_ONE_MINUS_SRC_COLOR -> GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR;
            case GL11.GL_DST_COLOR -> GlStateManager.DestFactor.DST_COLOR;
            case GL11.GL_ONE_MINUS_DST_COLOR -> GlStateManager.DestFactor.ONE_MINUS_DST_COLOR;
            case GL11.GL_SRC_ALPHA -> GlStateManager.DestFactor.SRC_ALPHA;
            case GL11.GL_ONE_MINUS_SRC_ALPHA -> GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
            case GL11.GL_DST_ALPHA -> GlStateManager.DestFactor.DST_ALPHA;
            case GL11.GL_ONE_MINUS_DST_ALPHA -> GlStateManager.DestFactor.ONE_MINUS_DST_ALPHA;
            case GL14.GL_CONSTANT_COLOR -> GlStateManager.DestFactor.CONSTANT_COLOR;
            case GL14.GL_ONE_MINUS_CONSTANT_COLOR -> GlStateManager.DestFactor.ONE_MINUS_CONSTANT_COLOR;
            case GL14.GL_CONSTANT_ALPHA -> GlStateManager.DestFactor.CONSTANT_ALPHA;
            case GL14.GL_ONE_MINUS_CONSTANT_ALPHA -> GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA;
            default -> GlStateManager.DestFactor.ONE;
        };
    }

    /**
     * 设置float类型的uniform变量
     */
    public PostShader setFloat(String name, float value) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(value);
        }
        return this;
    }

    /**
     * 设置vec2类型的uniform变量
     */
    public PostShader setVector2(String name, float x, float y) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(x, y);
        }
        return this;
    }

    /**
     * 设置vec3类型的uniform变量
     */
    public PostShader setVector3(String name, float x, float y, float z) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(x, y, z);
        }
        return this;
    }

    /**
     * 设置vec4类型的uniform变量
     */
    public PostShader setVector4(String name, float x, float y, float z, float w) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(x, y, z, w);
        }
        return this;
    }

    /**
     * 设置int类型的uniform变量
     */
    public PostShader setInt(String name, int value) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(value);
        }
        return this;
    }

    /**
     * 设置mat4类型的uniform变量
     */
    public PostShader setMatrix4(String name, Matrix4f matrix) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(matrix);
        }
        return this;
    }

    /**
     * 设置sampler类型的uniform变量（纹理）
     * @param name sampler的名称
     * @param textureId 纹理ID
     */
    public PostShader setSampler(String name, int textureId) {
        this.effect.setSampler(name, () -> textureId);
        return this;
    }

    /**
     * 设置sampler类型的uniform变量（使用RenderTarget）
     * @param name sampler的名称
     * @param renderTarget 渲染目标
     */
    public PostShader setSampler(String name, RenderTarget renderTarget) {
        if (renderTarget != null) {
            this.effect.setSampler(name, renderTarget::getColorTextureId);
        }
        return this;
    }
}
