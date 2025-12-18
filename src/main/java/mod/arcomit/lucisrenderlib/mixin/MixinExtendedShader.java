package mod.arcomit.lucisrenderlib.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import mod.arcomit.lucisrenderlib.client.ClientConfig;
import mod.arcomit.lucisrenderlib.client.renderer.postprocessor.RenderTypePostProcessor;
import mod.arcomit.lucisrenderlib.client.renderer.rendertarget.HDRTarget;
import mod.arcomit.lucisrenderlib.client.renderer.rendertarget.HDRTargetPool;
import mod.arcomit.lucisrenderlib.client.util.RenderStateHelper;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-11-21 12:10
 * @Description:  Mixin ExtendedShader以重定向帧缓冲区到我们的自定义HDR目标
 */
@Mixin(value = ExtendedShader.class, remap = false)
public class MixinExtendedShader {

    @Shadow
    @Final
    private GlFramebuffer writingToBeforeTranslucent;

    @Shadow
    @Final
    private GlFramebuffer writingToAfterTranslucent;

    @Shadow
    @Final
    private IrisRenderingPipeline parent;

    @Inject(method = "apply", at = @At("TAIL"))
    private void redirectToCustomFramebuffer(CallbackInfo ci) {
        if (ClientConfig.ENABLES_RENDER_TYPE_POST_PROCESSING.get()) {
            if (lucisrenderlib$shouldUsePostProcessor()) {
                HDRTarget target = HDRTargetPool.acquireHDRTarget(RenderTypePostProcessor.HDR_TARGET_ID);

                RenderStateHelper.cacheCurrentState();
                target.clear(Minecraft.ON_OSX);
                RenderStateHelper.restoreCachedState();

                int fbo;
                if (this.parent.isBeforeTranslucent) {
                    fbo = writingToBeforeTranslucent.getId();
                } else {
                    fbo = writingToAfterTranslucent.getId();
                }
                target.copyDepthFrom(fbo, GlStateManager. Viewport.width(), GlStateManager. Viewport.height());

                target.bindWrite(false);
            }
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void redirectToOriginalFramebuffer(CallbackInfo ci) {
        if (ClientConfig.ENABLES_RENDER_TYPE_POST_PROCESSING.get()) {
            if (lucisrenderlib$shouldUsePostProcessor()) {
                HDRTarget target = HDRTargetPool.acquireHDRTarget(RenderTypePostProcessor.HDR_TARGET_ID);
                target.unbindWrite();

                GlFramebuffer original;
                if (this.parent.isBeforeTranslucent) {
                    original = writingToBeforeTranslucent;
                } else {
                    original = writingToAfterTranslucent;
                }
                original.bind();

                if (RenderTypePostProcessor.CURRENT_POST_SHADER != null) {
                    GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
                    RenderTypePostProcessor.CURRENT_POST_SHADER.setSampler("SourceSampler", target.getColorTextureId());
                    RenderTypePostProcessor.CURRENT_POST_SHADER.draw();
                    target.copyDepthTo(original.getId(), GlStateManager. Viewport.width(), GlStateManager. Viewport.height());
                    original.bind();
                }
            }
        }
    }

    @Unique
    private boolean lucisrenderlib$shouldUsePostProcessor() {
        return RenderTypePostProcessor.CURRENT_POST_SHADER != null
                && ! ShadowRenderingState.areShadowsCurrentlyBeingRendered();
//        return false;
    }
}