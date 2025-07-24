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

        ENABLES = getBlockFromId(GroundLevelingConfigs.SERVER.ENABLES.get());
        TREES = getBlockFromId(GroundLevelingConfigs.SERVER.TREES.get());
        GRASSES = getBlockFromId(GroundLevelingConfigs.SERVER.GRASSES.get());
        BLACK_LIST = getBlockFromId(GroundLevelingConfigs.SERVER.BLACK_LIST.get());
        LEAVES = getBlockFromId(leavesBlocks);
    }

    private static Set<Block> getBlockFromId(List<? extends String> lists) {
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
