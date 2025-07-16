package stan.ripto.groundleveling.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import stan.ripto.groundleveling.GroundLeveling;

public class ENUSLanguageProvider extends LanguageProvider {
    public ENUSLanguageProvider(PackOutput output) {
        super(output, GroundLeveling.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(TranslateKeys.KEY_DESCRIPTION, "Mode Change");
        add(TranslateKeys.KEY_CATEGORY, "Ground leveling");
        add(TranslateKeys.COMMAND_RESULT_SUCCESS, "Configuration reloaded successfully");
        add(TranslateKeys.COMMAND_RESULT_FAIL, "Failed to reload configuration");
        add(TranslateKeys.CAPABILITY_MODE_OFF, "Mode -> OFF");
        add(TranslateKeys.CAPABILITY_MODE_MATERIAL_VEIN_MINING, "Mode -> Material Vein Mining");
        add(TranslateKeys.CAPABILITY_MODE_GROUND_LEVELING, "Mode -> Ground Leveling");
        add(TranslateKeys.MESSAGE_MODE_CHANGE_OFF, "Mode -> OFF");
        add(TranslateKeys.MESSAGE_MODE_CHANGE_MATERIAL_VEIN_MINING, "Mode -> Material Vein Mining");
        add(TranslateKeys.MESSAGE_MODE_CHANGE_GROUND_LEVELING, "Mode -> Ground Leveling");
    }
}
