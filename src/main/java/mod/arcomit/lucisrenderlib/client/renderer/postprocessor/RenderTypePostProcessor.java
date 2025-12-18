package mod.arcomit.lucisrenderlib.client.renderer.postprocessor;

import com.mojang.blaze3d.platform.GlStateManager;
import mod.arcomit.lucisrenderlib.client.ClientConfig;
import mod.arcomit.lucisrenderlib.client.renderer.pass.PostShader;
import mod.arcomit.lucisrenderlib.client.renderer.rendertarget.HDRTarget;
import mod.arcomit.lucisrenderlib.client.renderer.rendertarget.HDRTargetPool;
import mod.arcomit.lucisrenderlib.client.util.IrisHelper;
import mod.arcomit.lucisrenderlib.client.util.RenderStateHelper;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-12 19:57
 * @Description: 渲染类型后处理
 */
public class RenderTypePostProcessor {
    public static PostShader CURRENT_POST_SHADER;
    public static final String HDR_TARGET_ID = "render_type_post_processor_target";

    private static int cachedFbo = -1;

    public static void bindPostShader(PostShader shader) {
        CURRENT_POST_SHADER = shader;
        if (ClientConfig.ENABLES_RENDER_TYPE_POST_PROCESSING.get()) {
            if (!IrisHelper.irisIsLoadedAndShaderPackon()) {
                cachedFbo = GlStateManager.getBoundFramebuffer();
                HDRTarget target = HDRTargetPool.acquireHDRTarget(HDR_TARGET_ID);

                RenderStateHelper.cacheCurrentState();
                target.clear(Minecraft.ON_OSX);
                RenderStateHelper.restoreCachedState();

                target.copyDepthFrom(cachedFbo, GlStateManager. Viewport.width(), GlStateManager. Viewport.height());

                target.bindWrite(false);
            }
        }
    }

    public static void unbindPostShader() {
        if (ClientConfig.ENABLES_RENDER_TYPE_POST_PROCESSING.get()) {
            if (!IrisHelper.irisIsLoadedAndShaderPackon()) {
                HDRTarget target = HDRTargetPool.acquireHDRTarget(HDR_TARGET_ID);
                target.unbindWrite();

                GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, cachedFbo);

                if (CURRENT_POST_SHADER != null) {
                    GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
                    CURRENT_POST_SHADER.setSampler("SourceSampler", target.getColorTextureId());
                    CURRENT_POST_SHADER.draw();
                    target.copyDepthTo(cachedFbo, GlStateManager. Viewport.width(), GlStateManager. Viewport.height());
                    GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, cachedFbo);
                }
            }
        }
        CURRENT_POST_SHADER = null;
    }

}
