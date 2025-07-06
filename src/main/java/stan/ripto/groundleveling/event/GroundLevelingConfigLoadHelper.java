package stan.ripto.groundleveling.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroundLevelingConfigLoadHelper {
    public static void loadConfig() {
        List<? extends String> breakableBlocks = GroundLevelingConfigs.getBreakableBlock();
        List<? extends String> breakableTreeBlocks = GroundLevelingConfigs.getTreeBreakableBlock();

        GroundLevelingForgeEvents.setEnables(setIds(breakableBlocks));
        GroundLevelingForgeEvents.setTrees(setIds(breakableTreeBlocks));
        GroundLevelingForgeEvents.setLeaves(setIds("#minecraft:leaves"));
        GroundLevelingForgeEvents.setOres(setIds("#forge:ores"));
    }

    private static Set<Block> setIds(List<? extends String> list) {
        Set<Block> ids = new HashSet<>();

        for (String l : list) {
            ResourceLocation location;
            if (l.startsWith("#")) {
                location = ResourceLocation.parse(l.substring(1));
                TagKey<Block> key = TagKey.create(ForgeRegistries.Keys.BLOCKS, location);
                ITagManager<Block> manager = ForgeRegistries.BLOCKS.tags();
                if (manager != null) {
                    manager.getTag(key).forEach(ids::add);
                }
            } else {
                location = ResourceLocation.parse(l);
                Block b = ForgeRegistries.BLOCKS.getValue(location);
                if (b != null) {
                    ids.add(b);
                }
            }
        }

        return ids;
    }

    private static Set<Block> setIds(String loc) {
        Set<Block> ids = new HashSet<>();

        ResourceLocation location;
        if (loc.startsWith("#")) {
            location = ResourceLocation.parse(loc.substring(1));
            TagKey<Block> key = TagKey.create(ForgeRegistries.Keys.BLOCKS, location);
            ITagManager<Block> manager = ForgeRegistries.BLOCKS.tags();
            if (manager != null) {
                manager.getTag(key).forEach(ids::add);
            }
        } else {
            location = ResourceLocation.parse(loc);
            Block b = ForgeRegistries.BLOCKS.getValue(location);
            if (b != null) {
                ids.add(b);
            }
        }
        return ids;
    }
}
