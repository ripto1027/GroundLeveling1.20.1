package stan.ripto.groundleveling.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilitySerializer;

import java.util.function.Supplier;

public class GroundLevelingSyncPacket {
    private final int mode;

    public GroundLevelingSyncPacket(int mode) {
        this.mode = mode;
    }

    public static void encode(GroundLevelingSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.mode);
    }

    public static GroundLevelingSyncPacket decode(FriendlyByteBuf buf) {
        return new GroundLevelingSyncPacket(buf.readInt());
    }

    public static void handle(GroundLevelingSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            player.getCapability(GroundLevelingCapabilitySerializer.INSTANCE)
                    .ifPresent(data -> data.setMode(packet.mode));
        });
        context.get().setPacketHandled(true);
    }
}
