package mod.arcomit.lucisrenderlib.client.util;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraftforge.fml.ModList;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-08 17:06
 * @Description: Iris辅助类
 */
public class IrisHelper {
    public static boolean isLoadedIris = false;

    public static boolean irisIsLoadedAndShaderPackon() {
        if (isLoadedIris) {
            return IrisApi.getInstance().isShaderPackInUse();
        }
        return false;
    }

}
