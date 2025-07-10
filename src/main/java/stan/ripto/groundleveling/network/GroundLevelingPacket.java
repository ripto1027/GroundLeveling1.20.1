package stan.ripto.groundleveling.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilitySerializer;

import java.util.function.Supplier;

public class GroundLevelingPacket {
    public GroundLevelingPacket() {}

    public static void encode(@SuppressWarnings("unused") GroundLevelingPacket packet,@SuppressWarnings("unused") FriendlyByteBuf buf) {}

    public static GroundLevelingPacket decode(@SuppressWarnings("unused") FriendlyByteBuf buf) {
        //noinspection InstantiationOfUtilityClass
        return new GroundLevelingPacket();
    }

    public static void handle(@SuppressWarnings("unused") GroundLevelingPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            player.getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(data -> {
                data.changeMode();
                int currentMode = data.getMode();
                GroundLevelingNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GroundLevelingSyncPacket(currentMode));
            });
        });
        context.get().setPacketHandled(true);
    }
}
