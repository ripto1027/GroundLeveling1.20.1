package stan.ripto.groundleveling.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class GroundLevelingConfigs {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ServerConfigs SERVER;

    static {
        final Pair<ServerConfigs, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ServerConfigs::new);

        SERVER_SPEC = pair.getRight();
        SERVER = pair.getLeft();
    }

    public static class ServerConfigs {
        public final ForgeConfigSpec.IntValue BREAK_PER_TICK;
        public final ForgeConfigSpec.IntValue WIDTH;
        public final ForgeConfigSpec.IntValue HEIGHT;
        public final ForgeConfigSpec.IntValue DEPTH;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> DISABLES;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> TREES;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> GRASSES;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;

        ServerConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("server");
            BREAK_PER_TICK = builder.defineInRange("BlockBreakCountPerTick", 64, 1, 1024);
            WIDTH = builder.defineInRange("WidthRadius", 3, 1, 15);
            HEIGHT = builder.defineInRange("Height", 5, 1, 16);
            DEPTH = builder.defineInRange("Depth", 16, 1, 256);
            builder.comment("Block registration for each list supports both IDs and tags");
            DISABLES = builder
                    .comment("You don't need to add blocks with block entities or fluid blocks to the blacklist")
                    .defineList(
                            "GroundLevelingModeBlackList",

                            List.of(),

                            o -> o instanceof String
                    );

            TREES = builder
                    .defineList(
                            "TreesFellingWhiteList",

                            List.of(
                                    "#minecraft:logs",
                                    "#minecraft:leaves",
                                    "minecraft:mushroom_stem",
                                    "minecraft:brown_mushroom_block",
                                    "minecraft:red_mushroom_block",
                                    "minecraft:nether_wart_block",
                                    "minecraft:warped_wart_block"
                            ),

                            o -> o instanceof String
                    );

            GRASSES = builder
                    .defineList(
                            "MowingWhiteList",

                            List.of(
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
                            ),

                            o -> o instanceof String
                    );

            BLACKLIST = builder
                    .defineList(
                            "ChainBreakerModeBlackList",

                            List.of(
                                    "minecraft:grass_block",
                                    "minecraft:dirt",
                                    "#forge:sand",
                                    "#forge:sandstone",
                                    "minecraft:gravel",
                                    "#forge:stone",
                                    "minecraft:netherrack",
                                    "minecraft:basalt"
                            ),

                            o -> o instanceof String
                    );

            builder.pop();
        }
    }
}
