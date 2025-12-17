package mod.arcomit.lucisrenderlib.client.renderer.rendertarget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import mod.arcomit.lucisrenderlib.client.util.RenderStateHelper;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;


/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-08 15:23
 * @Description:  HDRTarget 对象池，方便复用HDR目标，避免频繁创建和销毁
 */
public class HDRTargetPool {
    private static final Deque<HDRTarget> FREE_TARGETS = new ArrayDeque<>();
    private static final Map<String, HDRTarget> ACTIVE_TARGETS = new HashMap<>();

    private static int lastWidth, lastHeight;

    public static HDRTarget acquireHDRTarget(String id) {
        return ACTIVE_TARGETS.computeIfAbsent(id,
                key -> FREE_TARGETS.isEmpty() ? createNewTarget() : FREE_TARGETS.pop());
    }

    public static void releaseAllHDRTargets() {
        RenderTarget mainTarget = getActiveRenderTarget();
        final int currentWidth = mainTarget.width;
        final int currentHeight = mainTarget.height;

        // 检测分辨率变化
        boolean resolutionChanged = (lastWidth != currentWidth || lastHeight != currentHeight);
        lastWidth = currentWidth;
        lastHeight = currentHeight;

        // 回收所有活跃目标
        FREE_TARGETS.addAll(ACTIVE_TARGETS.values());
        ACTIVE_TARGETS.clear();
        FREE_TARGETS.forEach(target -> {
            target.clear(Minecraft.ON_OSX);
            if (resolutionChanged) {
                target.resize(currentWidth, currentHeight, Minecraft.ON_OSX);
            }
        });
    }

    public static void releaseHDRTarget(String id) {
        HDRTarget target = ACTIVE_TARGETS.remove(id);
        if (target != null) {
            FREE_TARGETS.push(target);
            target.clear(Minecraft.ON_OSX);
        }
    }

    private static HDRTarget createNewTarget() {
        return createHDRTarget(
                getActiveRenderTarget()
        );
    }

    // 创建与目标同尺寸的HDR目标
    public static HDRTarget createHDRTarget(RenderTarget target) {
        HDRTarget rendertarget = new HDRTarget(target.width, target.height);
        rendertarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        rendertarget.clear(Minecraft.ON_OSX);
        return rendertarget;
    }

    public static RenderTarget getActiveRenderTarget(){
        return Minecraft.getInstance().getMainRenderTarget();
    }
}
