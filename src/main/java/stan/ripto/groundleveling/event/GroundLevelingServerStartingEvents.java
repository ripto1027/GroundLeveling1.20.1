package stan.ripto.groundleveling.event;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.command.GroundLevelingConfigLoadCommand;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GroundLevelingServerStartingEvents {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        GroundLevelingConfigLoadCommand.register(event.getServer().getCommands().getDispatcher());
    }
}
