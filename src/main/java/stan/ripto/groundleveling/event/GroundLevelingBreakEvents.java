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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;

import java.util.*;

public class GroundLevelingBreakEvents {
    private static Set<Block> enable;
    private static final Set<Block> trees = GroundLevelingAddReloadListenerEvent.getTrees();
    private static final Set<Block> leaves = GroundLevelingAddReloadListenerEvent.getLeaves();
    private static Direction face;
    private static int width;
    private static int height;
    private static int depth;
    private static boolean isInProgress = false;
    private static int exp;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!ClientSetup.getToggle() || isInProgress) return;

        isInProgress = true;
        Player p = event.getPlayer();
        Level l = p.level();
        face = ClickedFaceRecorderEvents.CLICK_FACE.get(p.getUUID());
        if (l.isClientSide() || !(p instanceof ServerPlayer player) || !(l instanceof ServerLevel level) || p.isShiftKeyDown() || face == Direction.DOWN || face == Direction.UP) {
            isInProgress = false;
            return;
        }

        enable = GroundLevelingAddReloadListenerEvent.getEnables();
        width = GroundLevelingConfigs.getWidth();
        height = GroundLevelingConfigs.getHeight();
        depth = GroundLevelingConfigs.getDepth();
        BlockPos origin = event.getPos();
        ItemStack tool = player.getMainHandItem();
        Block originBlock = level.getBlockState(origin).getBlock();
        if (!enable.contains(originBlock)) {
            isInProgress = false;
            return;
        }

        if (isLogs(level, origin)) {
            treeBreaker(level, origin, player, tool);
        } else {
            rangeBreaker(level, origin, player, tool);
        }

        isInProgress = false;
    }

    public static boolean destroyBlock(ServerLevel level, BlockPos pos, ServerPlayer player) {
        BlockState state = level.getBlockState(pos);
        int fortune = player.getMainHandItem().getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        int silk = player.getMainHandItem().getEnchantmentLevel(Enchantments.SILK_TOUCH);
        exp = state.getExpDrop(level, level.random, pos, fortune, silk);
        if (exp == -1) {
            return false;
        } else {
            BlockEntity blockentity = level.getBlockEntity(pos);
            Block block = state.getBlock();
            if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
                level.sendBlockUpdated(pos, state, state, 3);
                return false;
            } else if (player.getMainHandItem().onBlockStartBreak(pos, player)) {
                return false;
            } else if (player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) {
                return false;
            } else {
                if (player.isCreative()) {
                    removeBlock(level, pos, player, state, false);
                } else {
                    ItemStack stack = player.getMainHandItem();
                    ItemStack stackCopy = stack.copy();
                    boolean flag1 = state.canHarvestBlock(level, pos, player);
                    stack.mineBlock(level, state, pos, player);
                    if (stack.isEmpty() && !stackCopy.isEmpty())
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, stackCopy, InteractionHand.MAIN_HAND);
                    boolean flag = removeBlock(level, pos, player, state, flag1);

                    if (flag && flag1) {
                        block.playerDestroy(level, player, pos, state, blockentity, stackCopy);
                    }
                }
                return true;
            }
        }
    }

    private static boolean removeBlock(ServerLevel level, BlockPos pos, Player player, BlockState state, boolean canHarvest) {
        boolean removed = state.onDestroyedByPlayer(level, pos, player, canHarvest, level.getFluidState(pos));
        if (removed)
            state.getBlock().destroy(level, pos, state);
        return removed;
    }

    private static void dropsWarp(BlockPos pos, ServerLevel level, ServerPlayer player) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        AABB box = new AABB(pos).inflate(0.5);
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, box);
        drops.forEach(drop -> {
            drop.setPos(x, y, z);
            drop.setPickUpDelay(0);
        });

        if (exp > 0) {
            ExperienceOrb orb = new ExperienceOrb(level, x, y, z, exp);
            level.addFreshEntity(orb);
        }
    }

    private static boolean isLogs(ServerLevel level, BlockPos pos) {
        return trees.contains(level.getBlockState(pos).getBlock());
    }

    private static void rangeBreaker(ServerLevel level, BlockPos origin, ServerPlayer player, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(origin);

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            if (tool.isEmpty() || !tool.isDamageableItem() || tool.getDamageValue() >= tool.getMaxDamage()) break;

            if (destroyBlock(level, current, player)) {
                dropsWarp(current, level, player);
            } else {
                continue;
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

    private static boolean isRangeBreakable(BlockPos pos, Player player, BlockState state) {
        return enable.contains(state.getBlock()) && !(pos.getY() < player.getY()) && !player.isShiftKeyDown() && !player.isCreative() && ForgeHooks.isCorrectToolForDrops(state, player);
    }

    private static void treeBreaker(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack tool) {
        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current == null) continue;

            if (!tool.isDamageableItem() || tool.getDamageValue() >= tool.getMaxDamage()) {
                if (!tool.isEmpty()) break;
            }

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

    private static boolean isTreeBreakable(Player player, Block block) {
        return trees.contains(block) && !player.isCreative() && !player.isShiftKeyDown();
    }
}
