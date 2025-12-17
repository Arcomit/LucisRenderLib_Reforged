package mod.arcomit.lucisrenderlib.client.renderer.rendertarget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang. blaze3d.systems.RenderSystem;
import lombok.Getter;
import mod.arcomit.lucisrenderlib.client.util.RenderStateHelper;
import net.minecraft.client. Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api. distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import org.lwjgl.opengl. GL30;
import org.lwjgl.opengl.GL30C;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-05 18:21
 * @Description: HDR 渲染目标 - 使用 GL_RGBA16F 格式支持 HDR 颜色，
 * 根据GPL3.0 许可
 * 参考https://github.com/Low-Drag-MC/LDLib2/blob/1.21/src/main/java/com/lowdragmc/lowdraglib2/client/shader/HDRTarget.java实现
 * 原作者：KilaBash
 */
@OnlyIn(Dist.CLIENT)
public class HDRTarget extends RenderTarget {

    @Getter
    private int attachedDepthTexture = -1;

    // 缓存深度格式信息，避免重复查询
    private boolean cachedUseStencil = false;
    private boolean cachedUseCombined = false;

    public HDRTarget(int width, int height) {
        this(width, height, GL30.GL_NEAREST, true);
    }

    public HDRTarget(int width, int height, int filterMode, boolean useDepth) {
        super(useDepth);
        this.filterMode = filterMode;
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(width, height, Minecraft.ON_OSX);
    }

    @Override
    public void createBuffers(int width, int height, boolean clearError) {
        RenderSystem.assertOnRenderThreadOrInit();
        int maxSize = RenderSystem.maxSupportedTextureSize();

        if (width <= 0 || height <= 0 || width > maxSize || height > maxSize) {
            throw new IllegalArgumentException(
                    "Window " + width + "x" + height +
                            " size out of bounds (max.  size: " + maxSize + ")"
            );
        }

        // 设置尺寸
        this.viewWidth = width;
        this. viewHeight = height;
        this.width = width;
        this.height = height;

        // 创建帧缓冲和纹理
        this.frameBufferId = GlStateManager.glGenFramebuffers();
        this.colorTextureId = TextureUtil.generateTextureId();

        // 创建深度缓冲
        if (this.useDepth) {
            createDepthBuffer();
        }

        // 创建颜色缓冲（HDR）
        createColorBuffer();

        // 附加到帧缓冲
        attachBuffersToFramebuffer();

        // 重置附加深度纹理
        this.attachedDepthTexture = -1;

        // 检查状态并清理
        this.checkStatus();
        this.clear(clearError);
        this.unbindRead();
    }

    /**
     * 创建深度缓冲纹理
     */
    private void createDepthBuffer() {
        this.depthBufferId = TextureUtil.generateTextureId();
        GlStateManager._bindTexture(this.depthBufferId);

        // 批量设置深度纹理参数
        setTextureParameters(GL30.GL_TEXTURE_2D, GL30.GL_NEAREST, GL30.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_COMPARE_MODE, GL30.GL_NONE);

        // 根据是否使用模板选择格式
        if (! isStencilEnabled()) {
            GlStateManager._texImage2D(
                    GL30.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT,
                    this.width, this. height, 0,
                    GL30.GL_DEPTH_COMPONENT, GL30.GL_FLOAT, null
            );
        } else {
            GlStateManager._texImage2D(
                    GL30.GL_TEXTURE_2D, 0, GL30.GL_DEPTH32F_STENCIL8,
                    this.width, this. height, 0,
                    GL30.GL_DEPTH_STENCIL,
                    GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null
            );
        }
    }

    /**
     * 创建 HDR 颜色缓冲纹理
     */
    private void createColorBuffer() {
        this.setFilterMode(this.filterMode, true);
        GlStateManager._bindTexture(this.colorTextureId);

        // 设置纹理参数
        GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);

        // 使用 16 位浮点格式支持 HDR
        GlStateManager._texImage2D(
                GL30.GL_TEXTURE_2D, 0, GL30. GL_RGBA16F,
                this. width, this.height, 0,
                GL30.GL_RGBA, GL30.GL_FLOAT, null
        );
    }

    /**
     * 将缓冲附加到帧缓冲对象
     */
    private void attachBuffersToFramebuffer() {
        GlStateManager._glBindFramebuffer(GL30. GL_FRAMEBUFFER, this.frameBufferId);

        // 附加颜色缓冲
        GlStateManager._glFramebufferTexture2D(
                GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                GL30.GL_TEXTURE_2D, this.colorTextureId, 0
        );

        // 附加深度缓冲
        if (this.useDepth) {
            attachDepthBufferInternal(
                    this.depthBufferId,
                    isStencilEnabled(),
                    getUseCombinedDepthStencilAttachment()
            );
        }
    }

    /**
     * 辅助方法：批量设置纹理参数
     */
    private void setTextureParameters(int target, int filterMode, int wrapMode) {
        GlStateManager._texParameter(target, GL30.GL_TEXTURE_MIN_FILTER, filterMode);
        GlStateManager._texParameter(target, GL30.GL_TEXTURE_MAG_FILTER, filterMode);
        GlStateManager._texParameter(target, GL30.GL_TEXTURE_WRAP_S, wrapMode);
        GlStateManager._texParameter(target, GL30. GL_TEXTURE_WRAP_T, wrapMode);
    }

    /**
     * 获取组合深度模板附加点配置
     */
    private boolean getUseCombinedDepthStencilAttachment() {
        try {
            return ForgeConfig.CLIENT.useCombinedDepthStencilAttachment.get();
        } catch (Exception e) {
            // 如果配置不存在，默认返回 true
            return true;
        }
    }

    public void setFilterMode(int filterMode, boolean force) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (force || filterMode != this.filterMode) {
            this.filterMode = filterMode;
            GlStateManager._bindTexture(this.colorTextureId);
            GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, filterMode);
            GlStateManager._texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, filterMode);
            GlStateManager._bindTexture(0);
        }
    }

    /**
     * 附加外部深度纹理（通过纹理 ID）
     * 会自动检测深度纹理的格式
     */
    public void attachDepthBuffer(int depthTexture) {
        // 保存当前纹理绑定状态
        int previousTextureBinding = GlStateManager._getInteger(GL30.GL_TEXTURE_BINDING_2D);

        // 绑定并查询深度纹理格式
        GlStateManager._bindTexture(depthTexture);
        int internalFormat = GlStateManager._getTexLevelParameter(
                GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_INTERNAL_FORMAT
        );

        // 恢复之前的纹理绑定
        GlStateManager._bindTexture(previousTextureBinding);

        // 根据格式判断是否使用模板
        boolean useStencil = isStencilFormat(internalFormat);
        boolean useCombinedDepthStencil = useStencil;

        // 缓存格式信息
        this.cachedUseStencil = useStencil;
        this.cachedUseCombined = useCombinedDepthStencil;

        attachDepthBufferInternal(depthTexture, useStencil, useCombinedDepthStencil);
    }

    /**
     * 检查是否为模板格式
     */
    private boolean isStencilFormat(int internalFormat) {
        return internalFormat == GL30.GL_DEPTH_STENCIL
                || internalFormat == GL30.GL_DEPTH32F_STENCIL8
                || internalFormat == GL30.GL_DEPTH24_STENCIL8;
    }

    /**
     * 附加外部深度纹理（从 RenderTarget）
     */
    public void attachDepthBuffer(RenderTarget srcTarget) {
        if (srcTarget == null) {
            throw new IllegalArgumentException("Source render target cannot be null");
        }

        attachDepthBufferInternal(
                srcTarget.getDepthTextureId(),
                srcTarget. isStencilEnabled(),
                getUseCombinedDepthStencilAttachment()
        );
    }

    /**
     * 内部方法：附加深度缓冲到帧缓冲
     */
    public void attachDepthBufferInternal(int depthTexture, boolean useStencil, boolean useCombinedDepthStencil) {
        GlStateManager._glBindFramebuffer(GL30. GL_FRAMEBUFFER, frameBufferId);

        if (! useStencil) {
            // 仅深度
            GlStateManager._glFramebufferTexture2D(
                    GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                    GL30.GL_TEXTURE_2D, depthTexture, 0
            );
        } else if (useCombinedDepthStencil) {
            // 组合深度+模板附加点
            GlStateManager._glFramebufferTexture2D(
                    GL30. GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                    GL30.GL_TEXTURE_2D, depthTexture, 0
            );
        } else {
            // 分离的深度和模板附加点
            GlStateManager._glFramebufferTexture2D(
                    GL30. GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                    GL30.GL_TEXTURE_2D, depthTexture, 0
            );
            GlStateManager._glFramebufferTexture2D(
                    GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT,
                    GL30. GL_TEXTURE_2D, depthTexture, 0
            );
        }

        attachedDepthTexture = depthTexture;
    }

    /**
     * 检查是否附加了外部深度纹理
     */
    public boolean hasOtherAttachedDepthTexture() {
        return attachedDepthTexture != -1 && attachedDepthTexture != this.depthBufferId;
    }

    /**
     * 恢复到自己的深度纹理
     */
    public void restoreDepthTexture() {
        if (hasOtherAttachedDepthTexture() && this.depthBufferId != -1) {
            attachDepthBufferInternal(
                    this.depthBufferId,
                    isStencilEnabled(),
                    getUseCombinedDepthStencilAttachment()
            );
            attachedDepthTexture = -1;
        }
    }

    /**
     * 内部方法：从帧缓冲复制数据到另一个帧缓冲
     */
    private void copyInternal(int readFboId, int drawFboId, int srcWidth, int srcHeight, int dstWidth, int ditHeight, int mask, int filter) {
        RenderSystem.assertOnRenderThreadOrInit();

        // 设置读取和绘制帧缓冲
        GlStateManager._glBindFramebuffer(GL30. GL_READ_FRAMEBUFFER, readFboId);
        GlStateManager._glBindFramebuffer(GL30. GL_DRAW_FRAMEBUFFER, drawFboId);

        // 执行 blit 操作
        GlStateManager._glBlitFrameBuffer(
                0, 0, srcWidth, srcHeight,
                0, 0, dstWidth, ditHeight,
                mask, filter
        );

        // 恢复帧缓冲绑定
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    /**
     * 内部方法：从其他帧缓冲复制数据
     */
    private void copyFromInternal(int readFboId, int fboWidth, int fboHeight, int mask, int filter) {
        copyInternal(readFboId, this.frameBufferId, fboWidth, fboHeight, this.width, this.height, GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST);
    }

    /**
     * 内部方法：复制数据到其他帧缓冲
     */
    private void copyToInternal(int writeFboId, int fboWidth, int fboHeight, int mask, int filter) {
        copyInternal(this.frameBufferId, writeFboId, this.width, this.height, fboWidth, fboHeight, GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST);
    }

    /**
     * 从指定帧缓冲复制深度数据
     */
    public void copyDepthFrom(int id, int fboWidth, int fboHeight) {
        copyFromInternal(id, fboWidth, fboHeight, GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST);
    }

    /**
     * 从另一个 RenderTarget 复制深度数据
     */
    @Override
    public void copyDepthFrom(RenderTarget otherTarget) {
        if (otherTarget == null) {
            return;
        }
        if (otherTarget.width <= 0 || otherTarget. height <= 0) {
            return;
        }
        copyDepthFrom(otherTarget.frameBufferId, otherTarget.width, otherTarget.height);
    }

    /**
     * 从指定帧缓冲复制颜色数据
     */
    public void copyColorFrom(int id, int fboWidth, int fboHeight) {
        copyFromInternal(id, fboWidth, fboHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);
    }

    /**
     * 从另一个 RenderTarget 复制颜色数据
     */
    public void copyColorFrom(RenderTarget otherTarget) {
        if (otherTarget == null) {
            return;
        }
        if (otherTarget.width <= 0 || otherTarget.height <= 0) {
            return;
        }
        copyColorFrom(otherTarget.frameBufferId, otherTarget.width, otherTarget. height);
    }

    /**
     * 从指定帧缓冲复制深度和颜色数据
     */
    public void copyDepthAndColorFrom(int id, int fboWidth, int fboHeight) {
        copyFromInternal(
                id, fboWidth, fboHeight,
                GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT,
                GL30.GL_NEAREST
        );
    }

    /**
     * 从另一个 RenderTarget 复制深度和颜色数据
     */
    public void copyDepthAndColorFrom(RenderTarget otherTarget) {
        if (otherTarget == null) {
            return;
        }
        if (otherTarget.width <= 0 || otherTarget.height <= 0) {
            return;
        }
        copyDepthAndColorFrom(otherTarget.frameBufferId, otherTarget.width, otherTarget.height);
    }

    /**
     * 将深度数据复制到指定帧缓冲
     */
    public void copyDepthTo(int id, int fboWidth, int fboHeight) {
        copyToInternal(id, fboWidth, fboHeight, GL30.GL_DEPTH_BUFFER_BIT, GL30.GL_NEAREST);
    }

    /**
     * 将深度数据复制到另一个 RenderTarget
     */
    public void copyDepthTo(RenderTarget otherTarget) {
        if (otherTarget == null) {
            return;
        }
        if (this.width <= 0 || this.height <= 0) {
            return;
        }
        copyDepthTo(otherTarget.frameBufferId, this.width, this.height);
    }

    /**
     * 将颜色数据复制到指定帧缓冲
     */
    public void copyColorTo(int id, int fboWidth, int fboHeight) {
        copyToInternal(id, fboWidth, fboHeight, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);
    }

    /**
     * 将颜色数据复制到另一个 RenderTarget
     */
    public void copyColorTo(RenderTarget otherTarget) {
        if (otherTarget == null) {
            return;
        }
        if (this.width <= 0 || this.height <= 0) {
            return;
        }
        copyColorTo(otherTarget.frameBufferId, this.width, this.height);
    }

    /**
     * 将深度和颜色数据复制到指定帧缓冲
     */
    public void copyDepthAndColorTo(int id, int fboWidth, int fboHeight) {
        copyToInternal(
                id, fboWidth, fboHeight,
                GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT,
                GL30.GL_NEAREST
        );
    }

    /**
     * 将深度和颜色数据复制到另一个 RenderTarget
     */
    public void copyDepthAndColorTo(RenderTarget otherTarget) {
        if (otherTarget == null) {
            return;
        }
        if (this.width <= 0 || this.height <= 0) {
            return;
        }
        copyDepthAndColorTo(otherTarget.frameBufferId, this.width, this.height);
    }

    @Override
    public void clear(boolean pClearError) {
        RenderStateHelper.cacheCurrentState();
        super.clear(pClearError);
        RenderStateHelper.restoreCachedState();
    }

    /**
     * 清理资源时恢复深度纹理状态
     */
    @Override
    public void destroyBuffers() {
        if (hasOtherAttachedDepthTexture()) {
            restoreDepthTexture();
        }
        attachedDepthTexture = -1;
        cachedUseStencil = false;
        cachedUseCombined = false;
        super.destroyBuffers();
    }
}