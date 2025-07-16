package stan.ripto.groundleveling.util;

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
import net.minecraftforge.event.level.BlockEvent;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;

import java.util.List;
import java.util.Set;

public class GroundLevelingBlockBreakEventHandler {
    private final Set<Block> enables;
    private final Set<Block> trees;
    private final Set<Block> leaves;
    private final Set<Block> ores;
    public final int width = GroundLevelingConfigs.WIDTH.get();
    public final int height = GroundLevelingConfigs.HEIGHT.get();
    public final int depth = GroundLevelingConfigs.DEPTH.get();

    public GroundLevelingBlockBreakEventHandler(Set<Block> enables, Set<Block> trees, Set<Block> leaves, Set<Block> ores) {
        this.enables = enables;
        this.trees = trees;
        this.leaves = leaves;
        this.ores = ores;
    }

    public void findBreakableBlocks(GroundLevelingTasks task, BlockPos origin, BlockEvent.BreakEvent event) {
        task.queue.add(origin);
        task.visited.add(origin);

        if (task.mode == 1) {
            event.setCanceled(true);
            if (trees.contains(task.level.getBlockState(origin).getBlock())) {
                findTrees(task);
            } else if (ores.contains(task.level.getBlockState(origin).getBlock())) {
                findOres(task);
            }
        } else if (task.mode == 2) {
            event.setCanceled(true);
            findEnables(task, origin);
        }
    }

    private void findEnables(GroundLevelingTasks task, BlockPos origin) {
        while (!task.queue.isEmpty()) {
            BlockPos current = task.queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);

                if (task.visited.contains(next)) continue;

                BlockState state = task.level.getBlockState(next);
                if (!isEnables(next, task.player, state)) continue;
                if (!isOutRange(task.player, origin, next, task.face)) continue;

                task.queue.add(next);
                task.visited.add(next);
            }
        }
        task.foundCopy();
    }

    private boolean isEnables(BlockPos pos, Player player, BlockState state) {
        return enables.contains(state.getBlock()) && !(pos.getY() < player.getY()) && player.hasCorrectToolForDrops(state);
    }

    private boolean isOutRange(Player player, BlockPos origin, BlockPos next, Direction face) {
        int dh = next.getY() - (int) Math.floor(player.getY());
        int dw = 0;
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
        return dw <= width && dh >= 0 && dh <= height && dd >= 0 && dd <= depth;
    }

    private void findTrees(GroundLevelingTasks task) {
        while (!task.queue.isEmpty()) {
            BlockPos current = task.queue.poll();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos next = current.offset(x, y, z);

                        if (task.visited.contains(next)) continue;

                        Block block = task.level.getBlockState(next).getBlock();
                        if (!isTreeBreakable(block)) continue;

                        task.queue.add(next);
                        task.visited.add(next);
                    }
                }
            }
        }
        task.foundCopy();
    }

    private boolean isTreeBreakable(Block block) {
        return trees.contains(block);
    }

    private void findOres(GroundLevelingTasks task) {
        while (!task.queue.isEmpty()) {
            BlockPos current = task.queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);

                if (task.visited.contains(next)) continue;

                Block block = task.level.getBlockState(next).getBlock();
                if (!isOreBreakable(block)) continue;

                task.queue.add(next);
                task.visited.add(next);
            }
        }
        task.foundCopy();
    }

    private boolean isOreBreakable(Block block) {
        return ores.contains(block);
    }

    public boolean destroyBlock(GroundLevelingTasks task, BlockPos pos) {
        BlockState blockstate = task.level.getBlockState(pos);
        GameType type = task.player.gameMode.getGameModeForPlayer();
        int exp = ForgeHooks.onBlockBreakEvent(task.level, type, task.player, pos);
        if (exp == -1) {
            return true;
        } else {
            Block block = blockstate.getBlock();
            if (block instanceof GameMasterBlock && !task.player.canUseGameMasterBlocks()) {
                task.level.sendBlockUpdated(pos, blockstate, blockstate, 3);
                return true;
            } else if (task.player.blockActionRestricted(task.level, pos, type)) {
                return true;
            } else {
                ItemStack itemStack = task.player.getMainHandItem();
                ItemStack itemStackCopy = itemStack.copy();
                boolean flag1 = blockstate.canHarvestBlock(task.level, pos, task.player);

                if (!leaves.contains(block)) itemStack.mineBlock(task.level, blockstate, pos, task.player);

                boolean flag2 = false;
                if (itemStack.isEmpty() && !itemStackCopy.isEmpty()) {
                    ForgeEventFactory.onPlayerDestroyItem(task.player, itemStackCopy, InteractionHand.MAIN_HAND);
                    flag2 = true;
                }
                boolean flag = removeBlock(task.level, pos, task.player, flag1);

                if (flag && flag1) {
                    playerDestroy(task.level, task.player, pos, blockstate, itemStackCopy);
                }

                if (flag && exp > 0) {
                    task.player.giveExperiencePoints(exp);
                }

                return flag2;
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
}
