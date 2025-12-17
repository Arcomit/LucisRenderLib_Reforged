package mod.arcomit.lucisrenderlib.example.event;

import mod.arcomit.lucisrenderlib.LucisRenderLib;
import mod.arcomit.lucisrenderlib.client.renderer.postprocessor.RenderTypePostProcessor;
import mod.arcomit.lucisrenderlib.example.ScreenshotCapture;
import mod.arcomit.lucisrenderlib.example.init.LrKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-09 00:34
 * @Description: TODO
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = LucisRenderLib.MODID)
public class ScreenshotEvent {
    @SubscribeEvent
    public static void onRenderStage(RenderHandEvent event) {

    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END) return;
        Player player = Minecraft.getInstance().player;
        if(player == null) return;

        while(LrKeys.SCREENSHOT.consumeClick()) {
            ScreenshotCapture.requestExport();
        }
    }
}
