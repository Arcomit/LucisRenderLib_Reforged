package mod.arcomit.lucisrenderlib.client.renderer.material;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import mod.arcomit.lucisrenderlib.client.renderer.rendertarget.HDRTarget;
import mod.arcomit.lucisrenderlib.client.renderer.rendertarget.HDRTargetPool;
import mod.arcomit.lucisrenderlib.client.util.RenderMatrixHelper;
import mod.arcomit.lucisrenderlib.client.util.RenderStateHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-08 14:44
 * @Description: 着色器材质类
 */
public class ShaderMaterial {
    @Getter protected final String name;

    @Getter protected EffectInstance effect;

    public ShaderMaterial(String name, EffectInstance effect) {
        this.name = name;
        this.effect = effect;
    }

    /**
     * 带参数配置的渲染方法
     * @param name 着色器材质的名称（唯一）
     * @param shaderLocation shader的目录，lucisrenderlib:example对应目录 assets/lucisrenderlib/shaders/program/example.json
     */
    public ShaderMaterial(String name, ResourceLocation shaderLocation) throws IOException {
        this(name, createEffectInstance(shaderLocation));
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
/*        // 缓冲当前的Shader
        ShaderInstance previousShader = RenderSystem.getShader();*/

        // 缓存当前渲染状态（绑定的FBO，视图窗口大小）
        RenderStateHelper.cacheCurrentState();

        // 绑定当前材质的HDRTarget（FBO）
        HDRTarget hdrTarget = HDRTargetPool.acquireHDRTarget(name);
        hdrTarget.bindWrite(false);

        // 设置着色器参数
        this.setMatrix4("ProjMat", RenderMatrixHelper.orthographic(hdrTarget));
        this.setVector2("OutSize", (float) hdrTarget.width, (float) hdrTarget.height);
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

        // 将内容绘制到HDRTarget上
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(0.0D, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex(hdrTarget.width, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex(hdrTarget.width, hdrTarget.height, 500.0D).endVertex();
        bufferbuilder.vertex(0.0D, hdrTarget.height, 500.0D).endVertex();
        BufferUploader.draw(bufferbuilder.end());

        // 恢复之前缓存的深度状态
        if (prevDepthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.depthMask(prevDepthMask);

        this.effect.clear();

        // 解绑当前材质的HDRTarget（FBO）
        hdrTarget.unbindWrite();

        // 恢复之前缓存的渲染状态
        RenderStateHelper.restoreCachedState();

/*        // 恢复之前缓存的Shader
        RenderSystem.setShader(() -> previousShader);*/
    }

    /**
     * 获取当前ShaderMaterial的纹理ID
     */
    public int getTextureId() {
        HDRTarget renderTarget = HDRTargetPool.acquireHDRTarget(name);
        if (renderTarget != null) {
            return renderTarget.getColorTextureId();
        }
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture missing = textureManager.getTexture(TextureManager.INTENTIONAL_MISSING_TEXTURE);
        return missing.getId();
    }

    /**
     * 设置float类型的uniform变量
     */
    public ShaderMaterial setFloat(String name, float value) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(value);
        }
        return this;
    }

    /**
     * 设置vec2类型的uniform变量
     */
    public ShaderMaterial setVector2(String name, float x, float y) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(x, y);
        }
        return this;
    }

    /**
     * 设置vec3类型的uniform变量
     */
    public ShaderMaterial setVector3(String name, float x, float y, float z) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(x, y, z);
        }
        return this;
    }

    /**
     * 设置vec4类型的uniform变量
     */
    public ShaderMaterial setVector4(String name, float x, float y, float z, float w) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(x, y, z, w);
        }
        return this;
    }

    /**
     * 设置int类型的uniform变量
     */
    public ShaderMaterial setInt(String name, int value) {
        if (this.effect.getUniform(name) != null) {
            this.effect.getUniform(name).set(value);
        }
        return this;
    }

    /**
     * 设置mat4类型的uniform变量
     */
    public ShaderMaterial setMatrix4(String name, Matrix4f matrix) {
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
    public ShaderMaterial setSampler(String name, int textureId) {
        this.effect.setSampler(name, () -> textureId);
        return this;
    }

    /**
     * 设置sampler类型的uniform变量（使用RenderTarget）
     * @param name sampler的名称
     * @param renderTarget 渲染目标
     */
    public ShaderMaterial setSampler(String name, RenderTarget renderTarget) {
        if (renderTarget != null) {
            this.effect.setSampler(name, renderTarget::getColorTextureId);
        }
        return this;
    }
}
