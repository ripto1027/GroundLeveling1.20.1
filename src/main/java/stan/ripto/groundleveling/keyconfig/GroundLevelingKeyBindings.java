package stan.ripto.groundleveling.keyconfig;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class GroundLevelingKeyBindings {
    private static final String LANGUAGE_KEY = "key.groudleveling.toggle_destroy";
    public static final KeyMapping TOGGLE_DESTROY = new KeyMapping(LANGUAGE_KEY, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "Ground Leveling");

    public static String getLanguageKey() {
        return LANGUAGE_KEY;
    }
}
