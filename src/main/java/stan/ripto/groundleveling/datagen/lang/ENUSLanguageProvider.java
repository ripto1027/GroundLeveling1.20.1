package stan.ripto.groundleveling.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.command.GroundLevelingConfigLoadCommand;
import stan.ripto.groundleveling.event.GroundLevelingForgeEvents;
import stan.ripto.groundleveling.key.GroundLevelingKeyBindings;
import stan.ripto.groundleveling.network.GroundLevelingSyncPacket;

public class ENUSLanguageProvider extends LanguageProvider {
    public ENUSLanguageProvider(PackOutput output) {
        super(output, GroundLeveling.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(GroundLevelingKeyBindings.KEY_MAPPING_DESCRIPTION_TRANSLATE_KEY, "Mode Change");
        add(GroundLevelingKeyBindings.KEY_MAPPING_CATEGORY_TRANSLATE_KEY, "Ground leveling");
        add(GroundLevelingConfigLoadCommand.COMMAND_MESSAGE_SUCCESS_KEY, "Configuration reloaded successfully");
        add(GroundLevelingConfigLoadCommand.COMMAND_MESSAGE_FAIL_KEY, "Failed to reload configuration");
        add(GroundLevelingSyncPacket.CAPABILITY_BREAKER_MODE_OFF_TRANSLATE_KEY, "Mode -> OFF");
        add(GroundLevelingSyncPacket.CAPABILITY_BREAKER_MODE_MATERIAL_VEIN_MINING_TRANSLATE_KEY, "Mode -> Material Vein Mining");
        add(GroundLevelingSyncPacket.CAPABILITY_BREAKER_MODE_GROUND_LEVELING_TRANSLATE_KEY, "Mode -> Ground Leveling");
        add(GroundLevelingForgeEvents.MESSAGE_CURRENT_MODE_OFF_TRANSLATE_KEY, "Mode -> OFF");
        add(GroundLevelingForgeEvents.MESSAGE_CURRENT_MODE_MATERIAL_VEIN_TRANSLATE_KEY, "Mode -> Material Vein Mining");
        add(GroundLevelingForgeEvents.MESSAGE_CURRENT_MODE_GROUND_LEVELING_TRANSLATE_KEY, "Mode -> Ground Leveling");
    }
}
