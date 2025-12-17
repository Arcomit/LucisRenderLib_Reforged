package mod.arcomit.lucisrenderlib.example.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.Setter;
import mod.arcomit.lucisrenderlib.LucisRenderLib;
import mod.arcomit.lucisrenderlib.example.ScreenshotCapture;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-07 17:25
 * @Description: TODO
 */
public class TestParticle  extends SingleQuadParticle {
    @Getter
    @Setter
    private ResourceLocation texture = LucisRenderLib.prefix("textures/particle/test.png");

    public TestParticle(
            ClientLevel pLevel,
            double startX,
            double startY,
            double startZ,
            double endX,
            double endY,
            double endZ
    ) {
        super(pLevel, startX, startY, startZ, endX, endY, endZ);
        this.xd = endX;
        this.yd = endY;
        this.zd = endZ;

        this.lifetime = 20;

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.alpha = 1.0f;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
        texturemanager.getTexture(texture).setFilter(false, true);
        RenderSystem.setShaderTexture(0, texture);

        float animatedTime = this.age + partialTicks;
        if (animatedTime > this.lifetime) return;

        float t = Math.min(1, animatedTime / this.lifetime * 2.5f);

        // 获取相机位置
        Vec3 cameraPos = camera.getPosition();
        float cameraX = (float)(this.x - cameraPos.x());
        float cameraY = (float)(this.y - cameraPos.y());
        float cameraZ = (float)(this.z - cameraPos.z());

        // 1. 创建基础方向向量
        Vector3f right = new Vector3f(cameraX, cameraY, cameraZ);
        Vector3f dir = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd);

        // 2. 计算当前长度（基于动画进度）
        dir.mul(t);

        right.cross(dir);
        right.normalize();

        // 4. 计算宽度衰减因子（后60%时间逐渐变窄）
        //float widthFactor = (float) Math.sqrt(Math.min(1, (this.lifetime - animatedTime) / this.lifetime * 2.5f));

        float p = (animatedTime) / this.lifetime;
        p = Math.max(0f, Math.min(1f, p));
        float widthFactor;
        float midFactor = 0.5f;
        if (p <= 0.2f) {
            widthFactor = 1f;
        } else if (p <= 0.4f) {
            float tMid = (p - 0.2f) / 0.2f;
            widthFactor = 1f + (midFactor - 1f) * tMid;
            widthFactor = 1F;
        } else if (p <= 0.75f) {
//            rCol = 0;
//            gCol = 0;
//            bCol = 0;
            widthFactor = midFactor;
        } else {
            float tEnd = (p - 0.75f) / 0.25f;
            widthFactor = midFactor * (1f - tEnd);
        }
        widthFactor = Math.max(0f, Math.min(1f, widthFactor));

        right.mul(1f * widthFactor);
        //right.mul(1f);

        Vector3f left = new Vector3f(right);
        left.mul(-1);

        Vector3f[] vertices = new Vector3f[4];
        vertices[0] = new Vector3f(left);
        vertices[1] = new Vector3f(right);
        vertices[2] = new Vector3f(dir).add(right);
        vertices[3] = new Vector3f(dir).add(left);

        for (int i = 0; i < 4; i++) {
            vertices[i].add(cameraX, cameraY, cameraZ);
        }

        int light = LightTexture.FULL_BRIGHT;

        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(this.getU0(), this.getV1())
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                //.overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                //.normal(0f, 0f, 1f)
                .endVertex();

        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(this.getU0(), this.getV0())
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                //.overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                //.normal(0f, 0f, 1f)
                .endVertex();

        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(this.getU1(), this.getV0())
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                //.overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                //.normal(0f, 0f, 1f)
                .endVertex();

        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(this.getU1(), this.getV1())
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                //.overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                //.normal(0f, 0f, 1f)
                .endVertex();
    }

    @Override
    public boolean shouldCull() {
        return false;  // 禁用视锥体剔除
    }

    @Override
    protected float getU0() {
        return 0;
    }

    @Override
    protected float getU1() {
        return 1;
    }

    @Override
    protected float getV0() {
        return 0;
    }

    @Override
    protected float getV1() {
        return 1;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType. PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel world,
                double startX,
                double startY,
                double startZ,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new TestParticle(world, startX, startY, startZ,  0, 0, 10);
        }
    }
}
