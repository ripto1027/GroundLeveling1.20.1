package stan.ripto.groundleveling.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilitySerializer;

import java.util.function.Supplier;

public class GroundLevelingModeChangePacket {
    public GroundLevelingModeChangePacket() {}

    public static void encode(@SuppressWarnings("unused") GroundLevelingModeChangePacket packet, @SuppressWarnings("unused") FriendlyByteBuf buf) {}

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static GroundLevelingModeChangePacket decode(@SuppressWarnings("unused") FriendlyByteBuf buf) {
        return new GroundLevelingModeChangePacket();
    }

    public static void handle(@SuppressWarnings("unused") GroundLevelingModeChangePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            player.getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(data -> {
                data.changeMode();
                int currentMode = data.getMode();

                GroundLevelingNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new GroundLevelingSyncPacket(currentMode)
                );
            });
        });
        context.get().setPacketHandled(true);
    }
}
