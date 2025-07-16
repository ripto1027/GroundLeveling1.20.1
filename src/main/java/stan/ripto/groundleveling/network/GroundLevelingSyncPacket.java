package stan.ripto.groundleveling.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilitySerializer;
import stan.ripto.groundleveling.datagen.lang.TranslateKeys;

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
            player.getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(data -> {
                data.setMode(packet.mode);
                int currentMode = data.getMode();
                if (currentMode == 0) {
                    player.displayClientMessage(Component.translatable(TranslateKeys.CAPABILITY_MODE_OFF), true);
                } else if (currentMode == 1) {
                    player.displayClientMessage(Component.translatable(TranslateKeys.CAPABILITY_MODE_MATERIAL_VEIN_MINING), true);
                } else {
                    player.displayClientMessage(Component.translatable(TranslateKeys.CAPABILITY_MODE_GROUND_LEVELING), true);
                }
            });
        });
        context.get().setPacketHandled(true);
    }
}
