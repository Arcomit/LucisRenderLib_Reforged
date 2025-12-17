package mod.arcomit.lucisrenderlib.example.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.arcomit.lucisrenderlib.LucisRenderLib;
import mod.arcomit.lucisrenderlib.builtin.init.ShaderMaterials;
import mod.arcomit.lucisrenderlib.example.init.LrRenderTypes;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-13 11:49
 * @Description: TODO
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = LucisRenderLib.MODID)
public class DriveRenderEvent {
    private static final ResourceLocation MODEL = LucisRenderLib.prefix("model/util/drive.obj");
    private static final ResourceLocation TEXTURE = LucisRenderLib.prefix("model/util/man.png");

    @SubscribeEvent
    public static void onRender(RenderOverrideEvent event) {
        String target = event.getTarget();
        if (target.equals("blade")) {
            //event.setGetRenderType(LrRenderTypes::getSlashBladeBlendLuminous);
            event.setCanceled(true);

            ResourceLocation texture = event.getTexture();
            TextureManager texturemanager = Minecraft.getInstance().getTextureManager();

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = event.getBuffer();
            int packedLightIn = event.getPackedLightIn();

            ShaderMaterials.dissolve.setSampler("DiffuseSampler", texturemanager.getTexture(TEXTURE).getId());
            //ShaderMaterials.dissolve.setVector4("ColorModulate", r, g, b, 1.0f);
            ShaderMaterials.dissolve.setVector4("ColorModulate", 1, 1, 1, 1.0f);
            ShaderMaterials.dissolve.draw();

            RenderType rt = LrRenderTypes.getSlashBladeBlendNoMipmap(ShaderMaterials.dissolve.getTextureId(),TEXTURE);
            VertexConsumer vb = buffer.getBuffer(rt);

            int color = FastColor.ARGB32.color(255, 255, 255, 255);

            WavefrontObject obj = BladeModelManager.getInstance()
                    .getModel(MODEL);

            obj.tessellateOnly(vb, poseStack, LightTexture.FULL_BRIGHT, color, "base");

            Face.resetAlphaOverride();
            Face.resetUvOperator();

            BladeRenderState.resetCol();
        }
    }
}
