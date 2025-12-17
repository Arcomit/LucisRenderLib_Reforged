package mod.arcomit.lucisrenderlib.client.renderer.rendertarget;

import mod.arcomit.lucisrenderlib.LucisRenderLib;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-12 12:23
 * @Description: 用于每帧渲染结束时释放HDR目标
 */
@Mod.EventBusSubscriber(modid = LucisRenderLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReleaseHDRTargetEvent {
    @SubscribeEvent
    public static void onRenderEnd(TickEvent.RenderTickEvent event) {
        //一帧渲染开始时释放HDR目标
        if (event.phase != TickEvent.Phase.START) return;
        HDRTargetPool.releaseAllHDRTargets();
    }
}
