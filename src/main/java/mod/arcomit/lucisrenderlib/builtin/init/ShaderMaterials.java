package mod.arcomit.lucisrenderlib.builtin.init;

import mod.arcomit.lucisrenderlib.LucisRenderLib;
import mod.arcomit.lucisrenderlib.client.renderer.material.ShaderMaterial;
import mod.arcomit.lucisrenderlib.client.renderer.pass.PostShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-08 16:57
 * @Description: TODO
 */
@Mod.EventBusSubscriber(modid = LucisRenderLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ShaderMaterials {
    public static ShaderMaterial dissolve;
    public static ShaderMaterial drive;
    public static PostShader test;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            // name不要重复，建议加上你的modid
            dissolve = new ShaderMaterial(LucisRenderLib.MODID +".dissolve", LucisRenderLib.prefix("dissolve"));
            drive = new ShaderMaterial(LucisRenderLib.MODID +".drive", LucisRenderLib.prefix("drive"));
            test = new PostShader(LucisRenderLib.prefix("test"));
            TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
            ResourceLocation texture = LucisRenderLib.prefix("textures/star_color.png");
            test.setSampler("StarColor", texturemanager.getTexture(texture).getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
