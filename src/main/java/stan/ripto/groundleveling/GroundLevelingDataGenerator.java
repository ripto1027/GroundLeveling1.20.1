package stan.ripto.groundleveling;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import stan.ripto.groundleveling.lang.ENUSLanguageProvider;
import stan.ripto.groundleveling.lang.JAJPLanguageProvider;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GroundLevelingDataGenerator {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new ENUSLanguageProvider(output));
        generator.addProvider(event.includeClient(), new JAJPLanguageProvider(output));
    }
}
