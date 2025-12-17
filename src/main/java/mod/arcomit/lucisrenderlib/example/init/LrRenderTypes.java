package mod.arcomit.lucisrenderlib.example.init;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import mod.arcomit.lucisrenderlib.builtin.init.ShaderMaterials;
import mod.arcomit.lucisrenderlib.client.renderer.TextureIdStateShard;
import mod.arcomit.lucisrenderlib.client.renderer.postprocessor.RenderTypePostProcessor;
import mod.arcomit.lucisrenderlib.client.renderer.rendertarget.HDRTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-11-25 14:51
 * @Description: TODO
 */
public class LrRenderTypes extends RenderStateShard {

    public LrRenderTypes(String pName, Runnable pSetupState, Runnable pClearState) {
        super(pName, pSetupState, pClearState);
    }

    private static final OutputStateShard TEST_TARGET = new OutputStateShard("test_target", () -> {
        RenderTypePostProcessor.bindPostShader(ShaderMaterials.test);
    }, () -> {
        RenderTypePostProcessor.unbindPostShader();
    });

    private static final Map<Integer, RenderType> TEST_BLADE_BLEND_CACHE = new HashMap<>();
    private static final Map<String, RenderType> TEST_BLADE_BLEND_NO_MIPMAP_CACHE = new HashMap<>();

    public static RenderType getSlashBladeBlend(int textureId) {
        return TEST_BLADE_BLEND_CACHE.computeIfAbsent(
                textureId,
                t -> {
                    RenderType.CompositeState state = RenderType.CompositeState.builder()
                            .setShaderState(RenderStateShard.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                            .setTextureState(new TextureIdStateShard(t,false))  // 设置自定义纹理状态
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                            .createCompositeState(true);
                    return RenderType.create(
                            "test_blade_blend_" + t,
                            DefaultVertexFormat.NEW_ENTITY,
                            VertexFormat.Mode.TRIANGLES,
                            256,
                            true,
                            false,
                            state
                    );
                }
        );
    }

    protected static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    /**
     * 用于RenderTarget纹理的渲染类型，不使用mipmap
     * RenderTarget通常不会生成mipmap，使用mipmap会导致渲染出黑色
     */
    public static RenderType getSlashBladeBlendNoMipmap(int textureId, ResourceLocation texture) {
        return TEST_BLADE_BLEND_NO_MIPMAP_CACHE.computeIfAbsent(
                texture.toString() + textureId,
                t -> {
                    RenderType.CompositeState state = RenderType.CompositeState.builder()
                            .setShaderState(RenderStateShard.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                            .setOutputState(TEST_TARGET)
                            .setTextureState(new TextureIdStateShard(textureId, true))  // blur=true, mipmap=false
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .setCullState(RenderStateShard.CULL)
                            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                            .createCompositeState(true);
                    return RenderType.create(
                            "test_blade_blend_no_mipmap_" + t + texture,
                            DefaultVertexFormat.NEW_ENTITY,
                            VertexFormat.Mode.TRIANGLES,
                            256,
                            true,
                            false,
                            state
                    );
                }
        );
    }

    private static final Map<ResourceLocation, RenderType> slashBladeBlendCache = new HashMap<>();
    public static RenderType getSlashBladeBlend(ResourceLocation texture) {
        return slashBladeBlendCache.computeIfAbsent(texture, t -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setOutputState(TEST_TARGET)
                    .setTextureState(new RenderStateShard.TextureStateShard(t, false, true))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(true);

            return RenderType.create("slashblade_blend_" + t, DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.TRIANGLES, 256, true, false, state);
        });
    }

    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_ADDITIVE_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard(
                    "lightning_additive_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    public static RenderType getSlashBladeBlendLuminous(ResourceLocation texture) {
        return slashBladeBlendCache.computeIfAbsent(texture, t -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setOutputState(TEST_TARGET)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setTextureState(new RenderStateShard.TextureStateShard(t, true, true))
                    .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false);

            return RenderType.create("slashblade_blend_luminous_" + t, DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.TRIANGLES, 256, false, true, state);
        });
    }
}
