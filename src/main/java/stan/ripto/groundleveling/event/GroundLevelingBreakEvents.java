package stan.ripto.groundleveling.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;

import java.util.*;

public class GroundLevelingBreakEvents {
    private static final Set<String> enable = GroundLevelingServerStartedEvents.getEnables();
    private static final Set<String> trees = GroundLevelingServerStartedEvents.getTrees();
    private static final Set<String> leaves = GroundLevelingServerStartedEvents.getLeaves();
    private static Direction face;
    private static final int width = GroundLevelingConfigs.getWidth();
    private static final int height = GroundLevelingConfigs.getHeight();
    private static final int depth = GroundLevelingConfigs.getDepth();

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!ClientSetup.getToggle()) return;

        Player p = event.getPlayer();
        Level l = p.level();
        if (l.isClientSide() || !(l instanceof ServerLevel level) || p.isCreative() || p.isShiftKeyDown()) return;

        BlockPos origin = event.getPos();
        boolean mode = isLogs(level, origin);
        ItemStack tool = p.getMainHandItem();
        event.setCanceled(true);

        if (mode) {
            if (!treeBreaker(level, origin, p, tool)) return;
        } else {
            if (!rangeBreaker(level, origin, p, tool)) return;
            face = ClickedFaceRecorderEvents.CLICK_FACE.get(p.getUUID());
            if (face == Direction.UP || face == Direction.DOWN) return;
        }

        if (mode) {
            afterFirstTreeBreaker(level, origin, p, tool);
        } else {
            afterFirstRangeBreaker(level, origin, p, tool);
        }
    }

    private static void breaker(ServerLevel level, BlockPos pos, Player player, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, tool);
        int fortune = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        int silkTouch = tool.getEnchantmentLevel(Enchantments.SILK_TOUCH);
        int exp = state.getExpDrop(level, level.random, pos, fortune, silkTouch);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        for (ItemStack drop : drops) {
            ItemEntity d = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), drop.copy());
            d.setPickUpDelay(0);
            level.addFreshEntity(d);
        }

        if (exp > 0) {
            ExperienceOrb orb = new ExperienceOrb(level, player.getX(), player.getY(), player.getZ(), exp);
            level.addFreshEntity(orb);
        }
    }

    private static void setDamage(ItemStack tool, Player player) {
        tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    private static boolean isLogs(ServerLevel level, BlockPos pos) {
        return trees.contains(level.getBlockState(pos).getBlock().getName().getString());
    }

    private static boolean rangeBreaker(ServerLevel level, BlockPos pos, Player player, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        String id = state.getBlock().getName().getString();

        breaker(level, pos, player, tool);
        setDamage(tool, player);

        return isRangeBreakable(id, pos, player, state);
    }

    private static void afterFirstRangeBreaker(ServerLevel level, BlockPos origin, Player player, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(origin);

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            breaker(level, current, player, tool);
            setDamage(tool, player);

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);

                if (visited.contains(next)) continue;

                BlockState state = level.getBlockState(next);
                String id = state.getBlock().getName().getString();
                if (!isRangeBreakable(id, next, player, state)) continue;

                int dw = 0;
                int dh = next.getY() - origin.getY();
                int dd = 0;
                switch (face) {
                    case EAST -> {
                        dw = Math.abs(next.getZ() - origin.getZ());
                        dd = origin.getX() - next.getX();
                    }
                    case WEST -> {
                        dw = Math.abs(next.getZ() - origin.getZ());
                        dd = next.getX() - origin.getX();
                    }
                    case SOUTH -> {
                        dw = Math.abs(next.getX() - origin.getX());
                        dd = origin.getZ() - next.getZ();
                    }
                    case NORTH ->  {
                        dw = Math.abs(next.getX() - origin.getX());
                        dd = next.getZ() - origin.getZ();
                    }
                }
                if (dw > width || dh < 0 || dh > height || dd < 0 || dd > depth) continue;

                visited.add(next);
                queue.add(next);
            }
        }
    }

    private static boolean isRangeBreakable(String id, BlockPos pos, Player player, BlockState state) {
        return enable.contains(id) && !(pos.getY() < player.getY()) && !player.isCreative() && !player.isShiftKeyDown() && ForgeHooks.isCorrectToolForDrops(state, player);
    }

    private static boolean treeBreaker(ServerLevel level, BlockPos pos, Player player, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        String id = state.getBlock().getName().getString();

        breaker(level, pos, player, tool);
        setDamage(tool, player);

        return isTreeBreakable(id, player);
    }

    private static void afterFirstTreeBreaker(ServerLevel level, BlockPos pos, Player player, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            String id = level.getBlockState(current).getBlock().getName().getString();
            breaker(level, current, player, tool);
            if (!leaves.contains(id)) setDamage(tool, player);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos next = current.offset(x, y, z);

                        if (visited.contains(next)) continue;

                        String nextId = level.getBlockState(next).getBlock().getName().getString();
                        if (!isTreeBreakable(nextId, player)) continue;

                        visited.add(next);
                        queue.add(next);
                    }
                }
            }
        }
    }

    private static boolean isTreeBreakable(String id, Player player) {
        return trees.contains(id) && !player.isCreative() && !player.isShiftKeyDown();
    }
}
