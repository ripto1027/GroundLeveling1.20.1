package stan.ripto.groundleveling;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.event.ClickedFaceRecorderEvents;
import stan.ripto.groundleveling.event.GroundLevelingBreakEvents;
import stan.ripto.groundleveling.event.ClientSetup;

@Mod(GroundLeveling.MOD_ID)
public class GroundLeveling
{
    public static final String MOD_ID = "groundleveling";

    public GroundLeveling(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(GroundLevelingBreakEvents::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(ClickedFaceRecorderEvents::onLeftClicked);
        bus.addListener(ClientSetup::onRegisterKeyMappings);
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::onKeyInput);
        context.registerConfig(ModConfig.Type.COMMON, GroundLevelingConfigs.COMMON_CONFIG);
    }
}
