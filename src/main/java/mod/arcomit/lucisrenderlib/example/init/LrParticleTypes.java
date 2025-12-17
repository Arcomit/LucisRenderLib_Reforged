package mod.arcomit.lucisrenderlib.example.init;

import mod.arcomit.lucisrenderlib.LucisRenderLib;
import mod.arcomit.lucisrenderlib.example.particle.TestParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * @Author: Arcomit
 * @CreateTime: 2025-12-07 17:34
 * @Description: TODO
 */
@Mod.EventBusSubscriber(modid = LucisRenderLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LrParticleTypes {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, LucisRenderLib.MODID);

    public static final RegistryObject<SimpleParticleType> TEST_PARTICLE = PARTICLE_TYPES.register(
            "test_particle",
            () -> new SimpleParticleType(true)
    );

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerParticleFactories(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(TEST_PARTICLE.get(), TestParticle.Provider::new);
    }

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
