package stan.ripto.groundleveling.event;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import stan.ripto.groundleveling.keyconfig.GroundLevelingKeyBindings;

public class ClientSetup {
    private ClientSetup() {}
    private static boolean toggle;
    private static final String MODE_CHANGE_MESSAGE_KEY = "message.groundleveling.mode_change";

    public static boolean getToggle() {
        return toggle;
    }

    public static String getModeChangeMessageKey() {
        return MODE_CHANGE_MESSAGE_KEY;
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(GroundLevelingKeyBindings.TOGGLE_DESTROY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (GroundLevelingKeyBindings.TOGGLE_DESTROY.isDown()) {
                toggle = !toggle;
                player.displayClientMessage(Component.translatable(MODE_CHANGE_MESSAGE_KEY, toggle ? "ON" : "OFF"), true);
            }
        }
    }
}
