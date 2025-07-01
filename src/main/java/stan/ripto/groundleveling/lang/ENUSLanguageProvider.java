package stan.ripto.groundleveling.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.command.GroundLevelingConfigLoadCommand;
import stan.ripto.groundleveling.event.GroundLevelingForgeEvents;
import stan.ripto.groundleveling.keyconfig.GroundLevelingKeyBindings;

public class ENUSLanguageProvider extends LanguageProvider {
    public ENUSLanguageProvider(PackOutput output) {
        super(output, GroundLeveling.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(GroundLevelingKeyBindings.getLanguageKey(), "Toggle Key");
        add(GroundLevelingConfigLoadCommand.getCommandMessageSuccessKey(), "Configuration reloaded successfully");
        add(GroundLevelingConfigLoadCommand.getCommandMessageFailKey(), "Failed to reload configuration");
        add(GroundLevelingForgeEvents.getKeyInputMessageTranslateKey(), "RangeBreakMode: %s");
    }
}
