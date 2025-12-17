package mod.arcomit.lucisrenderlib.client.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.joml.Matrix4f;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-08 17:06
 * @Description: 渲染矩阵辅助类
 */
public class RenderMatrixHelper {
    public static Matrix4f orthographic(RenderTarget target) {
        return new Matrix4f().setOrtho(0.0F, (float) target.width, 0.0F, (float) target.height, 0.1F, 1000.0F);
    }
}
