package stan.ripto.groundleveling.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilities;

import java.util.function.Supplier;

public class GroundLevelingInProcessingChangePacket {
    private final boolean inProcessing;

    public GroundLevelingInProcessingChangePacket(boolean inProcessing) {
        this.inProcessing = inProcessing;
    }

    public static void encode(GroundLevelingInProcessingChangePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.inProcessing);
    }

    public static GroundLevelingInProcessingChangePacket decode(FriendlyByteBuf buf) {
        return new GroundLevelingInProcessingChangePacket(buf.readBoolean());
    }

    public static void handle(GroundLevelingInProcessingChangePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            player.getCapability(GroundLevelingCapabilities.INSTANCE)
                    .ifPresent(data -> data.setInProcessing(packet.inProcessing));
        });
    }
}
