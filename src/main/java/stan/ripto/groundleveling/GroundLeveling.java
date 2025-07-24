package stan.ripto.groundleveling;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.event.GroundLevelingModEvents;

@Mod(GroundLeveling.MOD_ID)
public class GroundLeveling
{
    public static final String MOD_ID = "groundleveling";

    public GroundLeveling(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        bus.addListener(GroundLevelingModEvents::onRegisterKeyMapping);
        bus.addListener(GroundLevelingModEvents::onRegisterCapabilities);
        bus.addListener(GroundLevelingModEvents::onCommonSetup);
        context.registerConfig(ModConfig.Type.SERVER, GroundLevelingConfigs.SERVER_SPEC);
    }
}
