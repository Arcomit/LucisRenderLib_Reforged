package mod.arcomit.lucisrenderlib.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-08 16:36
 * @Description: 让你的RenderType能够使用textureId。（理论上还可以提供mipmap的设置，但HDR不支持）
 */
@OnlyIn(Dist.CLIENT)
public class TextureIdStateShard extends RenderStateShard.EmptyTextureStateShard {
    private final int textureId;
    protected boolean blur;

    public TextureIdStateShard(int textureId, boolean blur) {
        super(() -> {
            RenderSystem.bindTexture(textureId);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
                    blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
                    blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
            RenderSystem.setShaderTexture(0, textureId);
        }, () -> {
        });
        this.textureId = textureId;
    }

    @Override
    public String toString() {
        return this.name + "[" + this.textureId + "(blur=" + this.blur + ")]";
    }

}

