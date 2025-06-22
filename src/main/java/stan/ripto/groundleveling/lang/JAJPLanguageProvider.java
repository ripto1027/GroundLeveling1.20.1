package stan.ripto.groundleveling.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.keyconfig.GroundLevelingKeyBindings;

public class JAJPLanguageProvider extends LanguageProvider {
    public JAJPLanguageProvider(PackOutput output) {
        super(output, GroundLeveling.MOD_ID, "ja_jp");
    }

    @Override
    protected void addTranslations() {
        add(GroundLevelingKeyBindings.getLanguageKey(), "ON/OFF切り替え");
    }
}
