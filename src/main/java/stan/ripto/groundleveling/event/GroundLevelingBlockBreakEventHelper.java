package stan.ripto.groundleveling.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class GroundLevelingBlockBreakEventHelper {
    private final Set<Block> enables;
    private final Set<Block> trees;
    private final Set<Block> leaves;
    private final Set<Block> ores;
    private final Direction face;
    private final int width;
    private final int height;
    private final int depth;

    public GroundLevelingBlockBreakEventHelper(Set<Block> enables, Set<Block> trees, Set<Block> leaves, Set<Block> ores, Direction face, int width, int height, int depth) {
        this.enables = enables;
        this.trees = trees;
        this.leaves = leaves;
        this.ores = ores;
        this.face = face;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    private boolean destroyBlock(ServerLevel level, BlockPos pos, ServerPlayer player) {
        BlockState blockstate = level.getBlockState(pos);
        GameType type = player.gameMode.getGameModeForPlayer();
        int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(level, type, player, pos);
        if (exp == -1) {
            return false;
        } else {
            BlockEntity blockentity = level.getBlockEntity(pos);
            Block block = blockstate.getBlock();
            if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
                level.sendBlockUpdated(pos, blockstate, blockstate, 3);
                return false;
            } else if (player.blockActionRestricted(level, pos, type)) {
                return false;
            } else {
                if (player.isCreative()) {
                    removeBlock(level, pos, player, false);
                } else {
                    ItemStack itemStack = player.getMainHandItem();
                    ItemStack itemStackCopy = itemStack.copy();
                    boolean flag1 = blockstate.canHarvestBlock(level, pos, player);
                    itemStack.mineBlock(level, blockstate, pos, player);
                    if (itemStack.isEmpty() && !itemStackCopy.isEmpty())
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemStackCopy, InteractionHand.MAIN_HAND);
                    boolean flag = removeBlock(level, pos, player, flag1);

                    if (flag && flag1) {
                        block.playerDestroy(level, player, pos, blockstate, blockentity, itemStackCopy);
                    }

                    if (flag && exp > 0) {
                        ExperienceOrb orb = new ExperienceOrb(level, player.getX(), player.getY(), player.getZ(), exp);
                        level.addFreshEntity(orb);
                    }
                }
                return true;
            }
        }
    }

    private boolean removeBlock(ServerLevel level, BlockPos pos, ServerPlayer player, boolean canHarvest) {
        BlockState state = level.getBlockState(pos);
        boolean removed = state.onDestroyedByPlayer(level, pos, player, canHarvest, level.getFluidState(pos));
        if (removed)
            state.getBlock().destroy(level, pos, state);
        return removed;
    }

    private void dropsWarp(BlockPos pos, ServerLevel level, ServerPlayer player) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        AABB box = new AABB(pos).inflate(0.5);
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, box);
        drops.forEach(drop -> {
            drop.setPos(x, y, z);
            drop.setPickUpDelay(0);
        });
    }

    public void rangeBreaker(ServerLevel level, BlockPos origin, ServerPlayer player) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(origin);

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            if (destroyBlock(level, current, player)) {
                dropsWarp(current, level, player);
            } else {
                break;
            }

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);

                if (visited.contains(next)) continue;

                BlockState state = level.getBlockState(next);
                if (!isRangeBreakable(next, player, state)) continue;

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

    private boolean isRangeBreakable(BlockPos pos, Player player, BlockState state) {
        return enables.contains(state.getBlock()) && !(pos.getY() < player.getY()) && !player.isShiftKeyDown() && !player.isCreative() && player.hasCorrectToolForDrops(state);
    }

    public void treeBreaker(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            Block currentBlock = level.getBlockState(current).getBlock();
            if (destroyBlock(level, current, player)) dropsWarp(current, level, player);
            if (!leaves.contains(currentBlock)) tool.setDamageValue(tool.getDamageValue() + 1);

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

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            if (destroyBlock(level, current, player)) {
                dropsWarp(current, level, player);
            } else {
                break;
            }

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
