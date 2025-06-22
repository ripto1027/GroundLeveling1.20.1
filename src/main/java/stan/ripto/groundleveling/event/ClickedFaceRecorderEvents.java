package stan.ripto.groundleveling.event;

import net.minecraft.core.Direction;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;
import java.util.WeakHashMap;

public class ClickedFaceRecorderEvents {
    public static final WeakHashMap<UUID, Direction> CLICK_FACE = new WeakHashMap<>();

    @SubscribeEvent
    public static void onLeftClicked(PlayerInteractEvent.LeftClickBlock event) {
        Direction face = event.getFace();
        if (face != null) {
            CLICK_FACE.put(event.getEntity().getUUID(), face);
        }
    }
}
