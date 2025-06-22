package stan.ripto.groundleveling.keyconfig;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class GroundLevelingKeyBindings {
    public static final KeyMapping TOGGLE_DESTROY = new KeyMapping("key.groudleveling.toggle_destroy", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "Ground Leveling");
}
