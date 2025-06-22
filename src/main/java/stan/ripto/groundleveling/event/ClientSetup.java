package stan.ripto.groundleveling.event;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import stan.ripto.groundleveling.keyconfig.GroundLevelingKeyBindings;

public class ClientSetup {
    private ClientSetup() {}
    private static boolean toggle;

    public static boolean getToggle() {
        return toggle;
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(GroundLevelingKeyBindings.TOGGLE_DESTROY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().player != null) {
            if (GroundLevelingKeyBindings.TOGGLE_DESTROY.isDown()) {
                toggle = !toggle;
                Minecraft.getInstance().player.displayClientMessage(Component.literal("一括破壊: " + (toggle ? "ON" : "OFF")), true);
            }
        }
    }
}
