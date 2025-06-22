package stan.ripto.groundleveling.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import stan.ripto.groundleveling.GroundLeveling;

public class GroundLevelingTags {
    public static TagKey<Block> ENABLE = BlockTags.create(ResourceLocation.fromNamespaceAndPath(GroundLeveling.MOD_ID, "enable"));
}
