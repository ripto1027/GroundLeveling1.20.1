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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;

import java.util.List;

public class GroundLevelingBlockBreakEventHandler {
    private final int width = GroundLevelingConfigs.SERVER.WIDTH.get();
    private final int height = GroundLevelingConfigs.SERVER.HEIGHT.get();
    private final int depth = GroundLevelingConfigs.SERVER.DEPTH.get();

    public GroundLevelingBlockBreakEventHandler() {}

    public void findBreakableBlocks(GroundLevelingTasks task, BlockPos origin, BlockEvent.BreakEvent event) {
        event.setCanceled(true);

        task.queue.add(origin);
        task.found.add(origin);
        task.visited.add(origin);

        Block block = task.level.getBlockState(origin).getBlock();

        if (task.findType == 0) {
            findTrees(task);
        } else if (task.findType == 1) {
            findGrasses(task);
        } else if (task.findType == 2) {
            findChainBreakables(task, block);
        } else if (task.findType == 3) {
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
                if (!isEnables(next, task.player, state, task.level)) continue;
                if (!isOutRange(task.player, origin, next, task.face)) continue;

                task.queue.add(next);
                task.willSortList.add(next);
                task.visited.add(next);
            }
        }
        getSortedFound(task, origin);
    }

    private boolean isEnables(BlockPos pos, ServerPlayer player, BlockState state, ServerLevel level) {
        return !GroundLevelingConfigLoadHandler.DISABLES.contains(state.getBlock()) && !state.isAir() && level.getFluidState(pos).isEmpty() && !(pos.getY() < player.getY()) && player.hasCorrectToolForDrops(state) && !state.hasBlockEntity();
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

    private void getSortedFound(GroundLevelingTasks task, BlockPos origin) {
        BlockPosComparators comparator = new BlockPosComparators(task, origin);
        task.willSortList.sort(comparator.COM);
        task.found.addAll(task.willSortList);
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
                        if (!GroundLevelingConfigLoadHandler.TREES.contains(block)) continue;

                        task.queue.add(next);
                        task.found.add(next);
                        task.visited.add(next);
                    }
                }
            }
        }
    }

    private void findGrasses(GroundLevelingTasks task) {
        while (!task.queue.isEmpty()) {
            BlockPos current = task.queue.poll();

            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos next = current.offset(x, 0, z);

                    if (task.visited.contains(next)) continue;

                    Block block = task.level.getBlockState(next).getBlock();
                    if (!GroundLevelingConfigLoadHandler.GRASSES.contains(block)) continue;

                    task.queue.add(next);
                    task.found.add(next);
                    task.visited.add(next);
                }
            }
        }
    }

    private void findChainBreakables(GroundLevelingTasks task, Block originBlock) {
        while (!task.queue.isEmpty()) {
            BlockPos current = task.queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);

                if (task.visited.contains(next)) continue;

                BlockState state = task.level.getBlockState(next);
                if (!isChainBreakable(state, originBlock, task.player)) continue;

                task.queue.add(next);
                task.found.add(next);
                task.visited.add(next);
            }
        }
    }

    private boolean isChainBreakable(BlockState state, Block originBlock, Player player) {
        return originBlock == state.getBlock() && !GroundLevelingConfigLoadHandler.BLACKLIST.contains(state.getBlock()) && player.hasCorrectToolForDrops(state);
    }

    public boolean destroyBlock(GroundLevelingTasks task, BlockPos pos) {
        BlockState state = task.level.getBlockState(pos);

        if (!task.player.hasCorrectToolForDrops(state)) {
            return false;
        }

        GameType type = task.player.gameMode.getGameModeForPlayer();

        ItemStack itemStack = task.player.getMainHandItem();
        ItemStack itemStackCopy = itemStack.copy();

        if (!GroundLevelingConfigLoadHandler.LEAVES.contains(state.getBlock())) {
            itemStack.mineBlock(task.level, state, pos, task.player);
        }

        boolean isToolBroken = itemStack.isEmpty() && !itemStackCopy.isEmpty();
        if (isToolBroken) {
            ForgeEventFactory.onPlayerDestroyItem(task.player, itemStackCopy, InteractionHand.MAIN_HAND);
        }

        boolean isRemoved = removeBlock(task.level, pos, state, task.player);
        if (isRemoved) {
            popDrops(task.level, task.player, pos, state, itemStackCopy);
        }

        int exp = ForgeHooks.onBlockBreakEvent(task.level, type, task.player, pos);
        if (isRemoved && exp > 0) {
            task.player.giveExperiencePoints(exp);
        }

        return isRemoved && !isToolBroken;
    }

    private boolean removeBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
        boolean removed = state.onDestroyedByPlayer(level, pos, player, true, level.getFluidState(pos));

        if (removed) {
            state.getBlock().destroy(level, pos, state);
        }

        return removed;
    }

    private void popDrops(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, ItemStack tool) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, tool);

        drops.forEach(drop -> {
            ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), drop.copy());
            item.setNoPickUpDelay();
            level.addFreshEntity(item);
        });
    }
}
