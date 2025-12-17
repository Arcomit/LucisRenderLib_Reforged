package mod.arcomit.lucisrenderlib.client;

import mod.arcomit.lucisrenderlib.LucisRenderLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LucisRenderLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientConfig {
    // client
    public static ForgeConfigSpec.BooleanValue ENABLES_RENDER_TYPE_POST_PROCESSING;

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Render Type Post Processing Setting");
        ENABLES_RENDER_TYPE_POST_PROCESSING = builder.comment("Enable render type post processing?[Default:true]")
                .define("enablesRenderTypePostProcessing", true);
        builder.pop();
        SPEC = builder.build();
    }
}
