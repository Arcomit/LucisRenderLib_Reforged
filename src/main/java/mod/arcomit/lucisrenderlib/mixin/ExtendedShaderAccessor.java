package mod.arcomit.lucisrenderlib.mixin;

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.spongepowered.asm. mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-11-21 12:10
 * @Description:  Mixin ExtendedShader，以提供私有对象获取方法
 */
@Mixin(value = ExtendedShader. class, remap = false)
public interface ExtendedShaderAccessor {
    @Accessor
    GlFramebuffer getWritingToBeforeTranslucent();
    @Accessor
    GlFramebuffer getWritingToAfterTranslucent();
    @Accessor
    IrisRenderingPipeline getParent();
}