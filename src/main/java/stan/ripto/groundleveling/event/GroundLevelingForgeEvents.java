package stan.ripto.groundleveling.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilitySerializer;
import stan.ripto.groundleveling.command.GroundLevelingConfigLoadCommand;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.datagen.lang.TranslateKeys;
import stan.ripto.groundleveling.key.GroundLevelingKeyMappings;
import stan.ripto.groundleveling.network.GroundLevelingNetwork;
import stan.ripto.groundleveling.network.GroundLevelingModeChangePacket;
import stan.ripto.groundleveling.util.GroundLevelingBlockBreakEventHandler;
import stan.ripto.groundleveling.util.GroundLevelingConfigLoadHandler;
import stan.ripto.groundleveling.util.GroundLevelingSyncMode;
import stan.ripto.groundleveling.util.GroundLevelingTasks;

import java.util.*;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GroundLevelingForgeEvents {
    private static Set<Block> enables;
    private static Set<Block> trees;
    private static Set<Block> leaves;
    private static Set<Block> ores;
    private static Set<Block> grasses;
    public static final HashMap<UUID, Integer> mode = new HashMap<>();
    private static final HashMap<UUID, Direction> clickedFace = new HashMap<>();
    private static final HashMap<UUID, Boolean> inProcessing = new HashMap<>();
    private static final List<GroundLevelingTasks> ACTIVE_TASKS = new ArrayList<>();


    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        GroundLevelingConfigLoadCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GroundLevelingConfigLoadHandler.loadConfig();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        GroundLevelingConfigLoadHandler.loadConfig();
    }

    @SubscribeEvent
    public static void onLeftClicked(PlayerInteractEvent.LeftClickBlock event) {
        Direction face = event.getFace();
        if (face != null) {
            clickedFace.put(event.getEntity().getUUID(), face);
        }
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (GroundLevelingKeyMappings.TOGGLE_DESTROY.consumeClick()) {
                GroundLevelingNetwork.CHANNEL.sendToServer(new GroundLevelingModeChangePacket());
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Iterator<GroundLevelingTasks> iterator = ACTIVE_TASKS.iterator();

            while (iterator.hasNext()) {
                GroundLevelingTasks task = iterator.next();
                int blockPerTick = GroundLevelingConfigs.BREAK_PER_TICK.get();

                for (int i = 0; i < blockPerTick; i++) {
                    if (task.found.isEmpty()) {
                        inProcessing.put(task.player.getUUID(), false);
                        iterator.remove();
                        break;
                    }

                    BlockPos pos = task.found.poll();
                    if (!task.handler.destroyBlock(task, pos)) {
                        inProcessing.put(task.player.getUUID(), false);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUUID();
        if (mode.get(uuid) == 0 || inProcessing.get(uuid) || p.isShiftKeyDown() || p.isCreative()) return;

        if (mode.get(uuid) == 2 && (clickedFace.get(uuid) == Direction.UP || clickedFace.get(uuid) == Direction.DOWN)) return;

        Level l = p.level();
        if (!(p instanceof ServerPlayer player) || !(l instanceof ServerLevel level)) return;

        BlockPos origin = event.getPos();
        Block block = l.getBlockState(origin).getBlock();
        if (mode.get(uuid) == 1) {
            if (!trees.contains(block) && !ores.contains(block) && !grasses.contains(block)) {
                return;
            }
        } else {
            if (!enables.contains(block)) {
                return;
            }
        }

        inProcessing.put(player.getUUID(), true);

        GroundLevelingBlockBreakEventHandler handler =
                new GroundLevelingBlockBreakEventHandler(enables, trees, leaves, ores, grasses);

        GroundLevelingTasks tasks =
                new GroundLevelingTasks(player, level, clickedFace.get(player.getUUID()), mode.get(player.getUUID()));

        handler.findBreakableBlocks(tasks, origin, event);
        tasks.handler = handler;

        ACTIVE_TASKS.add(tasks);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    ResourceLocation.fromNamespaceAndPath(GroundLeveling.MOD_ID, "breaker_mode"),
                    new GroundLevelingCapabilitySerializer()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(o ->
                event.getEntity().getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(n ->
                        n.setMode(o.getMode())
                )
        );
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        GroundLevelingSyncMode.sync(event.getEntity());
        inProcessing.put(event.getEntity().getUUID(), false);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (mode.isEmpty()) return;
        int m = mode.get(player.getUUID());

        MutableComponent renderText;
        if (m == 0) {
            renderText = Component.translatable(TranslateKeys.GUI_MODE_RENDER_OFF);
        } else if (m == 1) {
            renderText = Component.translatable(TranslateKeys.GUI_MODE_RENDER_CHAIN_MINING);
        } else {
            renderText = Component.translatable(TranslateKeys.GUI_MODE_RENDER_GROUND_LEVELING);
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int x = 5;
        int y = mc.getWindow().getGuiScaledHeight() - 10;

        guiGraphics.drawString(mc.font, renderText, x, y, 0xFFFFFF, false);
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

    public static void setOres(Set<Block> blocks) {
        ores = blocks;
    }

    public static void setGrasses(Set<Block> blocks) {
        grasses = blocks;
    }
}
