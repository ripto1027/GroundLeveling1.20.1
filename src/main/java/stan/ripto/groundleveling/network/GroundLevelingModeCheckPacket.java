package stan.ripto.groundleveling.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilities;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GroundLevelingModeCheckPacket {
    public GroundLevelingModeCheckPacket() {}

    public static void encode(GroundLevelingModeCheckPacket packet, FriendlyByteBuf buf) {}

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static GroundLevelingModeCheckPacket decode(FriendlyByteBuf buf) {
        return new GroundLevelingModeCheckPacket();
    }

    public static void handle(GroundLevelingModeCheckPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            player.getCapability(GroundLevelingCapabilities.INSTANCE).ifPresent(data -> {
                int currentMode = data.getMode();

                GroundLevelingNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new GroundLevelingModeSyncPacket(currentMode)
                );
            });
        });
        context.get().setPacketHandled(true);
    }
}
