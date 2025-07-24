package stan.ripto.groundleveling.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroundLevelingConfigLoadHandler {
    public static Set<Block> ENABLES;
    public static Set<Block> TREES;
    public static Set<Block> GRASSES;
    public static Set<Block> BLACK_LIST;
    public static Set<Block> LEAVES;

    public static void load() {
        List<? extends String> leavesBlocks = List.of(
                "#minecraft:leaves",
                "minecraft:brown_mushroom_block",
                "minecraft:red_mushroom_block",
                "minecraft:nether_wart_block",
                "minecraft:warped_wart_block"
        );

        ENABLES = getBlocksFromId(GroundLevelingConfigs.SERVER.ENABLES.get());
        TREES = getBlocksFromId(GroundLevelingConfigs.SERVER.TREES.get());
        GRASSES = getBlocksFromId(GroundLevelingConfigs.SERVER.GRASSES.get());
        BLACK_LIST = getBlocksFromId(GroundLevelingConfigs.SERVER.BLACK_LIST.get());
        LEAVES = getBlocksFromId(leavesBlocks);
    }

    private static Set<Block> getBlocksFromId(List<? extends String> list) {
        Set<Block> blocks = new HashSet<>();

        for (String element : list) {
            ResourceLocation location;
            if (element.startsWith("#")) {
                location = ResourceLocation.parse(element.substring(1));
                TagKey<Block> key = TagKey.create(ForgeRegistries.Keys.BLOCKS, location);
                ITagManager<Block> manager = ForgeRegistries.BLOCKS.tags();
                if (manager != null) {
                    manager.getTag(key).forEach(blocks::add);
                }
            } else {
                location = ResourceLocation.parse(element);
                Block b = ForgeRegistries.BLOCKS.getValue(location);
                if (b != null) {
                    blocks.add(b);
                }
            }
        }

        return blocks;
    }
}
