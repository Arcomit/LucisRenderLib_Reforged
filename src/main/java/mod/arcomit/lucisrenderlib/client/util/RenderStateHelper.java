package mod.arcomit.lucisrenderlib.client.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-12 16:04
 * @Description: 渲染状态辅助类，用于缓存和恢复渲染状态
 */
public class RenderStateHelper {
    private static int cachedFbo = -1;
    private static int cachedViewportX = -1;
    private static int cachedViewportY = -1;
    private static int cachedViewportWidth = -1;
    private static int cachedViewportHeight = -1;

    public static void cacheCurrentState() {
        cachedFbo = GlStateManager.getBoundFramebuffer();
        cachedViewportX = GlStateManager. Viewport.x();
        cachedViewportY = GlStateManager.Viewport. y();
        cachedViewportWidth = GlStateManager. Viewport.width();
        cachedViewportHeight = GlStateManager.Viewport.height();
    }

    public static void restoreCachedState() {
        if (cachedViewportX != -1 || cachedViewportY != -1 || cachedViewportWidth != -1 || cachedViewportHeight != -1) {
            RenderSystem.viewport(cachedViewportX, cachedViewportY, cachedViewportWidth, cachedViewportHeight);
        }
        if (cachedFbo != -1) {
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
            if (cachedFbo == main.frameBufferId) {
                main.bindWrite(false);
            }else {
                GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, cachedFbo);
            }
        }
    }
}
