package stan.ripto.groundleveling.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.command.GroundLevelingConfigLoadCommand;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.keyconfig.GroundLevelingKeyBindings;

import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GroundLevelingForgeEvents {
    private static Set<Block> enables;
    private static Set<Block> trees;
    private static Set<Block> leaves;
    private static final WeakHashMap<UUID, Direction> clickedFace = new WeakHashMap<>();
    private static boolean toggle;
    private static final String KEY_INPUT_MESSAGE = "message.groundleveling.key_input";
    private static boolean isInProgress = false;

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        GroundLevelingConfigLoadCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GroundLevelingConfigLoadHelper.loadConfig();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        GroundLevelingConfigLoadHelper.loadConfig();
    }

    @SubscribeEvent
    public static void onLeftClicked(PlayerInteractEvent.LeftClickBlock event) {
        Direction face = event.getFace();
        if (face != null) {
            clickedFace.put(event.getEntity().getUUID(), face);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && GroundLevelingKeyBindings.TOGGLE_DESTROY.isDown()) {
            toggle = !toggle;
            player.displayClientMessage(Component.translatable(KEY_INPUT_MESSAGE, toggle ? "ON" : "OFF"), true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!toggle || isInProgress) return;

        isInProgress = true;
        Player p = event.getPlayer();
        Level l = p.level();
        Direction face = clickedFace.get(p.getUUID());
        if (l.isClientSide() || !(p instanceof ServerPlayer player) || !(l instanceof ServerLevel level) || p.isShiftKeyDown() || face == Direction.DOWN || face == Direction.UP) {
            isInProgress = false;
            return;
        }

        int width = GroundLevelingConfigs.getWidth();
        int height = GroundLevelingConfigs.getHeight();
        int depth = GroundLevelingConfigs.getDepth();
        BlockPos origin = event.getPos();
        ItemStack tool = player.getMainHandItem();
        Block originBlock = level.getBlockState(origin).getBlock();
        if (!enables.contains(originBlock)) {
            isInProgress = false;
            return;
        }

        GroundLevelingBlockBreakEventHelper helper = new GroundLevelingBlockBreakEventHelper(enables, trees, leaves, face, width, height, depth);

        if (trees.contains(originBlock)) {
            helper.treeBreaker(level, origin, player, tool);
        } else {
            helper.rangeBreaker(level, origin, player);
        }

        isInProgress = false;
    }

    public static void setEnables(Set<Block> blocks) {
        enables = blocks;
    }

    public static void setTrees(Set<Block> blocks) {
        trees = blocks;
    }

    public static void setLeaves(Set<Block> blocks) {
        leaves = blocks;
    }

    public static String getKeyInputMessageTranslateKey() {
        return KEY_INPUT_MESSAGE;
    }
}
