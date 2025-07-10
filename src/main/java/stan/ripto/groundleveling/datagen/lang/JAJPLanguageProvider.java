package stan.ripto.groundleveling.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.command.GroundLevelingConfigLoadCommand;
import stan.ripto.groundleveling.event.GroundLevelingForgeEvents;
import stan.ripto.groundleveling.key.GroundLevelingKeyBindings;
import stan.ripto.groundleveling.network.GroundLevelingSyncPacket;

public class JAJPLanguageProvider extends LanguageProvider {
    public JAJPLanguageProvider(PackOutput output) {
        super(output, GroundLeveling.MOD_ID, "ja_jp");
    }

    @Override
    protected void addTranslations() {
        add(GroundLevelingKeyBindings.KEY_MAPPING_DESCRIPTION_TRANSLATE_KEY, "モード切り替え");
        add(GroundLevelingKeyBindings.KEY_MAPPING_CATEGORY_TRANSLATE_KEY, "Ground Leveling");
        add(GroundLevelingConfigLoadCommand.COMMAND_MESSAGE_SUCCESS_KEY, "コンフィグのリロードに成功しました。");
        add(GroundLevelingConfigLoadCommand.COMMAND_MESSAGE_FAIL_KEY, "コンフィグのリロードに失敗しました。");
        add(GroundLevelingSyncPacket.CAPABILITY_BREAKER_MODE_OFF_TRANSLATE_KEY, "機能OFF");
        add(GroundLevelingSyncPacket.CAPABILITY_BREAKER_MODE_MATERIAL_VEIN_MINING_TRANSLATE_KEY, "一括破壊モードON");
        add(GroundLevelingSyncPacket.CAPABILITY_BREAKER_MODE_GROUND_LEVELING_TRANSLATE_KEY, "整地モードON");
        add(GroundLevelingForgeEvents.MESSAGE_CURRENT_MODE_OFF_TRANSLATE_KEY, "現在、GroundLevelingの機能はOFFになっています。");
        add(GroundLevelingForgeEvents.MESSAGE_CURRENT_MODE_MATERIAL_VEIN_TRANSLATE_KEY, "現在、GroundLevelingは一括破壊モードがONになっています。");
        add(GroundLevelingForgeEvents.MESSAGE_CURRENT_MODE_GROUND_LEVELING_TRANSLATE_KEY, "現在、GroundLevelingは整地モードがONになっています。");
    }
}
