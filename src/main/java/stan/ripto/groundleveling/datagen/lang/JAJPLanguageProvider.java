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
        add(TranslateKeys.CAPABILITY_MODE_OFF, "機能OFF");
        add(TranslateKeys.CAPABILITY_MODE_MATERIAL_VEIN_MINING, "一括破壊モードON");
        add(TranslateKeys.CAPABILITY_MODE_GROUND_LEVELING, "整地モードON");
        add(TranslateKeys.MESSAGE_MODE_CHANGE_OFF, "現在、GroundLevelingの機能はOFFになっています。");
        add(TranslateKeys.MESSAGE_MODE_CHANGE_MATERIAL_VEIN_MINING, "現在、GroundLevelingは一括破壊モードがONになっています。");
        add(TranslateKeys.MESSAGE_MODE_CHANGE_GROUND_LEVELING, "現在、GroundLevelingは整地モードがONになっています。");
    }
}
