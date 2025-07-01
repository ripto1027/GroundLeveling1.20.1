package stan.ripto.groundleveling.event;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.common.Mod;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.keyconfig.GroundLevelingKeyBindings;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID)
public class GroundLevelingModEvents {
    public static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(GroundLevelingKeyBindings.TOGGLE_DESTROY);
    }
}
