package stan.ripto.groundleveling.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GroundLevelingServerStartedEvents {
    private static Set<String> enables;
    private static Set<String> trees;
    private static Set<String> leaves;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        List<? extends String> breakableBlocks = GroundLevelingConfigs.getBreakableBlock();
        List<? extends String> breakableTreeBlocks = GroundLevelingConfigs.getTreeBreakableBlock();


        enables = setIds(breakableBlocks);
        trees = setIds(breakableTreeBlocks);
        leaves = setIds("#minecraft:leaves");
    }

    private static Set<String> setIds(List<? extends String> list) {
        Set<String> ids = new HashSet<>();

        for (String l : list) {
            ResourceLocation location;
            if (l.startsWith("#")) {
                location = ResourceLocation.parse(l.substring(1));
                TagKey<Block> key = TagKey.create(ForgeRegistries.Keys.BLOCKS, location);
                ITagManager<Block> manager = ForgeRegistries.BLOCKS.tags();
                if (manager != null) {
                    manager.getTag(key).forEach(b -> ids.add(b.getName().getString()));
                }
            } else {
                location = ResourceLocation.parse(l);
                Block b = ForgeRegistries.BLOCKS.getValue(location);
                if (b != null) {
                    ids.add(b.getName().getString());
                }
            }
        }

        return ids;
    }

    private static Set<String> setIds(String loc) {
        Set<String> ids = new HashSet<>();

        ResourceLocation location;
        if (loc.startsWith("#")) {
            location = ResourceLocation.parse(loc.substring(1));
            TagKey<Block> key = TagKey.create(ForgeRegistries.Keys.BLOCKS, location);
            ITagManager<Block> manager = ForgeRegistries.BLOCKS.tags();
            if (manager != null) {
                manager.getTag(key).forEach(b -> ids.add(b.getName().getString()));
            }
        } else {
            location = ResourceLocation.parse(loc);
            Block b = ForgeRegistries.BLOCKS.getValue(location);
            if (b != null) {
                ids.add(b.getName().getString());
            }
        }
        return ids;
    }

    public static Set<String> getEnables() {
        return enables;
    }

    public static Set<String> getTrees() {
        return trees;
    }

    public static Set<String> getLeaves() {
        return leaves;
    }
}
