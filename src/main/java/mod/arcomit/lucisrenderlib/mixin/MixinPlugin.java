package mod.arcomit.lucisrenderlib.mixin;

import mod.arcomit.lucisrenderlib.client.util.IrisHelper;
import net.irisshaders.iris.Iris;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-11-21 12:10
 * @Description:  Mixin配置，只在Iris加载时启用Mixin
 */
public class MixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (LoadingModList.get().getModFileById(Iris.MODID) != null){
            IrisHelper.isLoadedIris = true;
            return true;
        }

        return false;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {}

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {}

}
