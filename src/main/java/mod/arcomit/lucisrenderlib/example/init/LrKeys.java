package mod.arcomit.lucisrenderlib.example.init;

import com.mojang.blaze3d.platform.InputConstants;
import mod.arcomit.lucisrenderlib.LucisRenderLib;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-09 00:32
 * @Description: TODO
 */
@Mod.EventBusSubscriber(modid = LucisRenderLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LrKeys {
    public static final KeyMapping SCREENSHOT = new KeyMapping(
            "key."+ LucisRenderLib.MODID +".test",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_5,
            "category."+ LucisRenderLib.MODID
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SCREENSHOT);
    }
}
