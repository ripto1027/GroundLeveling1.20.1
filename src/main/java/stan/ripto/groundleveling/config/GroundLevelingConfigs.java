package stan.ripto.groundleveling.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class GroundLevelingConfigs {
    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec.IntValue WIDTH;
    private static final ForgeConfigSpec.IntValue HEIGHT;
    private static final ForgeConfigSpec.IntValue DEPTH;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BREAKABLE_BLOCKS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TREE_BREAKABLE_BLOCKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("mining");
        WIDTH = builder.comment("Width radius").defineInRange("width", 3, 1, 15);
        HEIGHT = builder.comment("Height range").defineInRange("height", 5, 1, 5);
        DEPTH = builder.comment("Depth range").defineInRange("depth", 16, 1, 256);
        BREAKABLE_BLOCKS = builder.comment("Breakable blocks list").defineList("breakableBlocks", List.of("#forge:cobblestone", "#forge:ores", "#forge:fence_gates", "#forge:fences", "#forge:glass", "#forge:gravel", "#forge:netherrack", "#forge:obsidian", "#forge:sand", "#forge:sandstone", "#forge:stone", "#forge:storage_blocks", "#minecraft:wool", "#minecraft:planks", "#minecraft:stone_bricks", "#minecraft:logs", "#minecraft:dirt", "#minecraft:bamboo_blocks", "#minecraft:stairs", "#minecraft:slabs", "#minecraft:walls", "#minecraft:ice", "#minecraft:leaves", "#minecraft:trapdoors", "#minecraft:beds", "#minecraft:nylium", "#minecraft:wart_blocks", "#minecraft:soul_fire_base_blocks", "#minecraft:snow", "#minecraft:terracotta", "minecraft:melon", "minecraft:pumpkin"), o -> o instanceof String);
        TREE_BREAKABLE_BLOCKS = builder.comment("Breakable tree breaker list").defineList("treeBreakerBreakableBlocks", List.of("#minecraft:logs", "#minecraft:leaves", "minecraft:mushroom_stem", "minecraft:brown_mushroom_block", "minecraft:red_mushroom_block"), o -> o instanceof String);
        builder.pop();
        COMMON_CONFIG = builder.build();
    }

    public static int getWidth() {
        return WIDTH.get();
    }

    public static int getHeight() {
        return HEIGHT.get();
    }

    public static int getDepth() {
        return DEPTH.get();
    }

    public static List<? extends String> getBreakableBlock() {
        return BREAKABLE_BLOCKS.get();
    }

    public static List<? extends String> getTreeBreakableBlock() {
        return TREE_BREAKABLE_BLOCKS.get();
    }
}
