package mod.arcomit.lucisrenderlib.example.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.arcomit.lucisrenderlib.LucisRenderLib;
import mod.arcomit.lucisrenderlib.builtin.init.ShaderMaterials;
import mod.arcomit.lucisrenderlib.client.renderer.material.ShaderMaterial;
import mod.arcomit.lucisrenderlib.client.renderer.pass.PostShader;
import mod.arcomit.lucisrenderlib.client.renderer.rendertarget.HDRTargetPool;
import mod.arcomit.lucisrenderlib.example.ScreenshotCapture;
import mod.arcomit.lucisrenderlib.example.init.LrKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-13 11:18
 * @Description: TODO
 */
@Mod.EventBusSubscriber(modid = LucisRenderLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DynamicShaderMaterial {

    @SubscribeEvent
    public static void onRenderEnd(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ShaderMaterials.drive.setFloat("GameTime", RenderSystem.getShaderGameTime());
        ShaderMaterials.drive.setFloat("Alpha", 1F);
        ShaderMaterials.drive.draw();
    }
}
