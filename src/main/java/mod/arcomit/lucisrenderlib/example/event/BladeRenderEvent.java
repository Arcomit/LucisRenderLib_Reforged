package mod.arcomit.lucisrenderlib.example.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.arcomit.lucisrenderlib.LucisRenderLib;
import mod.arcomit.lucisrenderlib.builtin.init.ShaderMaterials;
import mod.arcomit.lucisrenderlib.example.init.LrRenderTypes;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;

import java.util.Arrays;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-09 00:34
 * @Description: TODO
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = LucisRenderLib.MODID)
public class BladeRenderEvent {

    @SubscribeEvent
    public static void onRender(RenderOverrideEvent event) {
        ItemStack blade = event.getStack();
        String target = event.getTarget();
/*        if (blade.getItem() instanceof ItemSlashBlade && (target.equals("blade") || target.equals("sheath"))) {
            event.setCanceled(true);

            ResourceLocation texture = event.getTexture();
            TextureManager texturemanager = Minecraft.getInstance().getTextureManager();

            // 使用游戏时间计算颜色变化
            float gameTime = (float) (Minecraft.getInstance().level.getGameTime() % 200) / 200.0f;
            float r = (float) (Math.sin(gameTime * Math.PI * 2) * 0.5 + 0.5);
            float g = (float) (Math.sin(gameTime * Math.PI * 2 + Math.PI * 2 / 3) * 0.5 + 0.5);
            float b = (float) (Math.sin(gameTime * Math.PI * 2 + Math.PI * 4 / 3) * 0.5 + 0.5);

            // 先设置参数，再draw
            ShaderMaterials.dissolve.setSampler("DiffuseSampler", texturemanager.getTexture(texture).getId());
            //ShaderMaterials.dissolve.setVector4("ColorModulate", r, g, b, 1.0f);
            ShaderMaterials.dissolve.setVector4("ColorModulate", 1, 1, 1, 1.0f);
            ShaderMaterials.dissolve.draw();

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = event.getBuffer();
            int packedLightIn = event.getPackedLightIn();

            // 使用ShaderMaterial渲染出的纹理
            // 注意：RenderTarget纹理不包含mipmap，必须使用NoMipmap版本，否则会渲染成黑色
            RenderType rt = LrRenderTypes.getSlashBladeBlendNoMipmap(ShaderMaterials.dissolve.getTextureId(),texture);
            VertexConsumer vb = buffer.getBuffer(rt);

            // 关键：这里的color要设置为白色(255,255,255,255)，让纹理颜色完全通过
            int color = FastColor.ARGB32.color(255, 255, 255, 255);

            event.getModel().tessellateOnly(vb, poseStack, packedLightIn, color, target);

            Face.resetAlphaOverride();
            Face.resetUvOperator();

            BladeRenderState.resetCol();

        }*/
    }
}
