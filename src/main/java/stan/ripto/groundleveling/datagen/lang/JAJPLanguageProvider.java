package stan.ripto.groundleveling.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import stan.ripto.groundleveling.GroundLeveling;

public class JAJPLanguageProvider extends LanguageProvider {
    public JAJPLanguageProvider(PackOutput output) {
        super(output, GroundLeveling.MOD_ID, "ja_jp");
    }

    @Override
    protected void addTranslations() {
        add(TranslateKeys.KEY_DESCRIPTION, "モード切り替え");
        add(TranslateKeys.KEY_CATEGORY, "Ground Leveling");
        add(TranslateKeys.COMMAND_RESULT_SUCCESS, "コンフィグのリロードに成功しました。");
        add(TranslateKeys.COMMAND_RESULT_FAIL, "コンフィグのリロードに失敗しました。");
        add(TranslateKeys.GUI_MODE_RENDER_OFF, "Mode: OFF");
        add(TranslateKeys.GUI_MODE_RENDER_CHAIN_MINING, "Mode: 一括破壊");
        add(TranslateKeys.GUI_MODE_RENDER_GROUND_LEVELING, "Mode: 整地");
    }
}
