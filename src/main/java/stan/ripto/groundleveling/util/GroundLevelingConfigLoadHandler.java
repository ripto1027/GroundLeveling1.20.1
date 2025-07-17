package stan.ripto.groundleveling.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.event.GroundLevelingForgeEvents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroundLevelingConfigLoadHandler {
    public static void loadConfig() {
        List<? extends String> breakableBlocks = GroundLevelingConfigs.BREAKABLE_BLOCKS.get();
        List<? extends String> breakableTreeBlocks = GroundLevelingConfigs.TREE_BREAKABLE_BLOCKS.get();
        List<? extends String> breakableOreBlocks = GroundLevelingConfigs.ORES_BREAKABLE_BLOCKS.get();

        List<? extends String> leavesBlocks = List.of(
                "#minecraft:leaves",
                "minecraft:brown_mushroom_block",
                "minecraft:red_mushroom_block",
                "minecraft:nether_wart_block",
                "minecraft:warped_wart_block"
        );

        List<? extends String> grassesBlocks = List.of(
                "#minecraft:flowers",
                "minecraft:brown_mushroom",
                "minecraft:red_mushroom",
                "minecraft:crimson_fungus",
                "minecraft:warped_fungus",
                "minecraft:grass",
                "minecraft:fern",
                "minecraft:dead_bush",
                "minecraft:crimson_roots",
                "minecraft:warped_roots",
                "minecraft:nether_sprouts",
                "minecraft:tall_grass",
                "minecraft:large_fern"
        );

        GroundLevelingForgeEvents.setEnables(setIds(breakableBlocks));
        GroundLevelingForgeEvents.setTrees(setIds(breakableTreeBlocks));
        GroundLevelingForgeEvents.setLeaves(setIds(leavesBlocks));
        GroundLevelingForgeEvents.setOres(setIds(breakableOreBlocks));
        GroundLevelingForgeEvents.setGrasses(setIds(grassesBlocks));
    }

    private static Set<Block> setIds(List<? extends String> lists) {
        Set<Block> ids = new HashSet<>();

        for (String li : lists) {
            ResourceLocation location;
            if (li.startsWith("#")) {
                location = ResourceLocation.parse(li.substring(1));
                TagKey<Block> key = TagKey.create(ForgeRegistries.Keys.BLOCKS, location);
                ITagManager<Block> manager = ForgeRegistries.BLOCKS.tags();
                if (manager != null) {
                    manager.getTag(key).forEach(ids::add);
                }
            } else {
                location = ResourceLocation.parse(li);
                Block b = ForgeRegistries.BLOCKS.getValue(location);
                if (b != null) {
                    ids.add(b);
                }
            }
        }

        return ids;
    }
}
