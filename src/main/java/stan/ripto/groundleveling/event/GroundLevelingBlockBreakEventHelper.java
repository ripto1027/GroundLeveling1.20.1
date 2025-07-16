package stan.ripto.groundleveling.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;

import java.util.*;

public class GroundLevelingBlockBreakEventHelper {
    private final Set<Block> enables;
    private final Set<Block> trees;
    private final Set<Block> leaves;
    private final Set<Block> ores;
    private final Direction face;

    public GroundLevelingBlockBreakEventHelper(Set<Block> enables, Set<Block> trees, Set<Block> leaves, Set<Block> ores, Direction face) {
        this.enables = enables;
        this.trees = trees;
        this.leaves = leaves;
        this.ores = ores;
        this.face = face;
    }

    private boolean destroyBlock(ServerLevel level, BlockPos pos, ServerPlayer player) {
        BlockState blockstate = level.getBlockState(pos);
        GameType type = player.gameMode.getGameModeForPlayer();
        int exp = ForgeHooks.onBlockBreakEvent(level, type, player, pos);
        if (exp == -1) {
            return true;
        } else {
            Block block = blockstate.getBlock();
            if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
                level.sendBlockUpdated(pos, blockstate, blockstate, 3);
                return true;
            } else if (player.blockActionRestricted(level, pos, type)) {
                return true;
            } else {
                if (player.isCreative()) {
                    removeBlock(level, pos, player, false);
                } else {
                    ItemStack itemStack = player.getMainHandItem();
                    ItemStack itemStackCopy = itemStack.copy();
                    boolean flag1 = blockstate.canHarvestBlock(level, pos, player);

                    if (!leaves.contains(block)) itemStack.mineBlock(level, blockstate, pos, player);

                    if (itemStack.isEmpty() && !itemStackCopy.isEmpty())
                        ForgeEventFactory.onPlayerDestroyItem(player, itemStackCopy, InteractionHand.MAIN_HAND);
                    boolean flag = removeBlock(level, pos, player, flag1);

                    if (flag && flag1) {
                        playerDestroy(level, player, pos, blockstate, itemStackCopy);
                    }

                    if (flag && exp > 0) {
                        player.giveExperiencePoints(exp);
                    }
                }
                return false;
            }
        }
    }

    private boolean removeBlock(ServerLevel level, BlockPos pos, ServerPlayer player, boolean canHarvest) {
        BlockState state = level.getBlockState(pos);
        boolean removed = state.onDestroyedByPlayer(level, pos, player, canHarvest, level.getFluidState(pos));
        if (removed) state.getBlock().destroy(level, pos, state);
        return removed;
    }

    private void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, ItemStack tool) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, tool);
        drops.forEach(drop -> {
            ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), drop.copy());
            item.setNoPickUpDelay();
            level.addFreshEntity(item);
        });
    }

    public void rangeBreaker(ServerLevel level, BlockPos origin, ServerPlayer player) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(origin);

        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(origin);

        int width = GroundLevelingConfigs.WIDTH.get();
        int height = GroundLevelingConfigs.HEIGHT.get();
        int depth = GroundLevelingConfigs.DEPTH.get();

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            if (destroyBlock(level, current, player)) break;

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);

                if (visited.contains(next)) continue;

                BlockState state = level.getBlockState(next);
                if (!isRangeBreakable(next, player, state)) continue;

                int dw = 0;
                int dh = next.getY() - (int) Math.floor(player.getY());
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

    private boolean isRangeBreakable(BlockPos pos, Player player, BlockState state) {
        return enables.contains(state.getBlock()) && !(pos.getY() < player.getY()) && !player.isShiftKeyDown() && !player.isCreative() && player.hasCorrectToolForDrops(state);
    }

    public void treeBreaker(ServerLevel level, BlockPos pos, ServerPlayer player) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            if (destroyBlock(level, current, player)) break;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos next = current.offset(x, y, z);

                        if (visited.contains(next)) continue;

                        Block nextBlock = level.getBlockState(next).getBlock();
                        if (!isTreeBreakable(player, nextBlock)) continue;

                        visited.add(next);
                        queue.add(next);
                    }
                }
            }
        }
    }

    private boolean isTreeBreakable(Player player, Block block) {
        return trees.contains(block) && !player.isCreative() && !player.isShiftKeyDown();
    }

    public void oreBreaker(ServerLevel level, BlockPos pos, ServerPlayer player) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            if (destroyBlock(level, current, player)) break;

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);

                if (visited.contains(next)) continue;

                Block nextBlock = level.getBlockState(next).getBlock();
                if (!isOreBreakable(player, nextBlock)) continue;

                visited.add(next);
                queue.add(next);
            }
        }
    }

    private boolean isOreBreakable(ServerPlayer player, Block block) {
        return ores.contains(block) && !player.isCreative() && !player.isShiftKeyDown();
    }
}
