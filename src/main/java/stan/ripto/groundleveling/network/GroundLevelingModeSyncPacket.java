package stan.ripto.groundleveling.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilities;

import java.util.function.Supplier;

public class GroundLevelingModeSyncPacket {
    private final int mode;

    public GroundLevelingModeSyncPacket(int mode) {
        this.mode = mode;
    }

    public static void encode(GroundLevelingModeSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.mode);
    }

    public static GroundLevelingModeSyncPacket decode(FriendlyByteBuf buf) {
        return new GroundLevelingModeSyncPacket(buf.readInt());
    }

    public static void handle(GroundLevelingModeSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            player.getCapability(GroundLevelingCapabilities.INSTANCE)
                    .ifPresent(data -> {
                        data.setSynced(false);
                        data.setMode(packet.mode);
                        data.setSynced(true);
                    });
        });
        context.get().setPacketHandled(true);
    }
}
