package mod.arcomit.lucisrenderlib.example.test;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.Minecraft.ON_OSX;

// 方便复用渲染对象，避免频繁创建和销毁
public class TargetManager {
    private static final Deque<RenderTarget> freeTargets = new ArrayDeque<>();
    private static final Map<ResourceLocation, RenderTarget> activeTargets = new HashMap<>();

    private static int lastWidth, lastHeight;

    public static RenderTarget getTarget(ResourceLocation id) {
        return activeTargets.computeIfAbsent(id, key -> {
            RenderTarget renderTarget = freeTargets.isEmpty() ?
                    createNewTarget() :
                    freeTargets.pop();
            return renderTarget;
        });
    }

    public static RenderTarget getTarget(String id) {
        return getTarget(ResourceLocation.parse(id));
    }

    public static void releaseAll() {
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        final int currentWidth = mainTarget.width;
        final int currentHeight = mainTarget.height;

        // 检测分辨率变化
        boolean resolutionChanged = (lastWidth != currentWidth || lastHeight != currentHeight);
        lastWidth = currentWidth;
        lastHeight = currentHeight;

        // 回收所有活跃目标
        freeTargets.addAll(activeTargets.values());
        activeTargets.clear();
        freeTargets.forEach(target -> {
            target.clear(ON_OSX);
            if (resolutionChanged) {
                target.resize(currentWidth, currentHeight, ON_OSX);
            }
        });
    }

    public static void releaseTarget(ResourceLocation id) {
        RenderTarget target = activeTargets.remove(id);
        if (target != null) {
            freeTargets.push(target);
            target.clear(ON_OSX);
        }
    }

    private static RenderTarget createNewTarget() {
        return createTempTarget(
                Minecraft.getInstance().getMainRenderTarget()
        );
    }

    public static RenderTarget createTempTarget(RenderTarget screenTarget) {
        // 创建与屏幕目标同尺寸的纹理目标
        RenderTarget rendertarget = new TextureTarget(screenTarget.width, screenTarget.height, true, ON_OSX);
        // 设置透明背景色
        rendertarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        rendertarget.clear(ON_OSX);
        return rendertarget;
    }

    public static RenderTarget getActiveRenderTarget(){
//        if(Minecraft.getInstance().levelRenderer.transparencyChain == null){
//            return Minecraft.getInstance().getMainRenderTarget();
//        }
//        else {
//            return Minecraft.getInstance().levelRenderer.getParticlesTarget();
//        }
        return Minecraft.getInstance().getMainRenderTarget();
    }
}
