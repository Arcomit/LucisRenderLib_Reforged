package mod.arcomit.lucisrenderlib;

import com.mojang.logging.LogUtils;
import mod.arcomit.lucisrenderlib.client.ClientConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-08-02 16:48
 * @Description: MOD主类
 */
@SuppressWarnings("removal")
@Mod(LucisRenderLib.MODID)
public class LucisRenderLib {

    public static final String MODID = "lucisrenderlib";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LucisRenderLib() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

    }

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(LucisRenderLib.MODID, path);
    }
}
