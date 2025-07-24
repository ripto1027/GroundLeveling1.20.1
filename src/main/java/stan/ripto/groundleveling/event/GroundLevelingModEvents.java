package stan.ripto.groundleveling.event;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.capability.IGroundLevelingData;
import stan.ripto.groundleveling.key.GroundLevelingKeyMappings;
import stan.ripto.groundleveling.network.GroundLevelingNetwork;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID)
public class GroundLevelingModEvents {
    public static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(GroundLevelingKeyMappings.CHANGE_MODE);
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IGroundLevelingData.class);
    }

    public static void onCommonSetup(@SuppressWarnings("unused") FMLCommonSetupEvent event) {
        GroundLevelingNetwork.register();
    }
}
