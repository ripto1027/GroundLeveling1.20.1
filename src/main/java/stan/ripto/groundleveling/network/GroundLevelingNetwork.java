package stan.ripto.groundleveling.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import stan.ripto.groundleveling.GroundLeveling;

public class GroundLevelingNetwork {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    ResourceLocation.fromNamespaceAndPath(GroundLeveling.MOD_ID, "main"),
                    () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
            );

    @SuppressWarnings("UnusedAssignment")
    public static void register() {
        int id = 0;

        CHANNEL.registerMessage(
                id++,
                GroundLevelingModeChangePacket.class,
                GroundLevelingModeChangePacket::encode,
                GroundLevelingModeChangePacket::decode,
                GroundLevelingModeChangePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                GroundLevelingSyncPacket.class,
                GroundLevelingSyncPacket::encode,
                GroundLevelingSyncPacket::decode,
                GroundLevelingSyncPacket::handle
        );
    }
}
