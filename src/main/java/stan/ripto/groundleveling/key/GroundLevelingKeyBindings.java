package stan.ripto.groundleveling.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class GroundLevelingKeyBindings {
    public static final String KEY_MAPPING_DESCRIPTION_TRANSLATE_KEY = "key_mapping.groudleveling.description";
    public static final String KEY_MAPPING_CATEGORY_TRANSLATE_KEY = "key_mapping.groundleveling.category";
    public static final KeyMapping TOGGLE_DESTROY = new KeyMapping(KEY_MAPPING_DESCRIPTION_TRANSLATE_KEY, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KEY_MAPPING_CATEGORY_TRANSLATE_KEY);
}
