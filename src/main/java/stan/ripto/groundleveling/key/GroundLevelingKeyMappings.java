package stan.ripto.groundleveling.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;
import stan.ripto.groundleveling.datagen.lang.TranslateKeys;

public class GroundLevelingKeyMappings {
    public static final KeyMapping CHANGE_MODE =
            new KeyMapping(
                    TranslateKeys.KEY_DESCRIPTION,
                    KeyConflictContext.IN_GAME,
                    KeyModifier.NONE,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_G,
                    TranslateKeys.KEY_CATEGORY
            );
}
