package stan.ripto.groundleveling.util;

import net.minecraft.world.entity.player.Player;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilitySerializer;
import stan.ripto.groundleveling.event.GroundLevelingForgeEvents;

public class GroundLevelingSyncMode {
    public static void sync(Player player) {
        player.getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(data -> {
            int m = data.getMode();
            GroundLevelingForgeEvents.mode.put(player.getUUID(), m);
        });
    }
}
