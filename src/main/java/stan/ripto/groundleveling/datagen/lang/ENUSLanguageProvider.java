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
        add(TranslateKeys.GUI_MODE_RENDER_OFF, "Mode: OFF");
        add(TranslateKeys.GUI_MODE_RENDER_CHAIN_MINING, "Mode: Chain Mining");
        add(TranslateKeys.GUI_MODE_RENDER_GROUND_LEVELING, "Mode: Ground Leveling");
    }
}
