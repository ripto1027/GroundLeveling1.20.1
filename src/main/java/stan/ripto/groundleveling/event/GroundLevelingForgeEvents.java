package stan.ripto.groundleveling.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.LazyOptional;
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
import stan.ripto.groundleveling.capability.IGroundLevelingData;
import stan.ripto.groundleveling.command.GroundLevelingConfigLoadCommand;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.datagen.lang.TranslateKeys;
import stan.ripto.groundleveling.key.GroundLevelingKeyBindings;
import stan.ripto.groundleveling.network.GroundLevelingNetwork;
import stan.ripto.groundleveling.network.GroundLevelingPacket;
import stan.ripto.groundleveling.util.GroundLevelingBlockBreakEventHandler;
import stan.ripto.groundleveling.util.GroundLevelingConfigLoadHandler;
import stan.ripto.groundleveling.util.GroundLevelingTasks;

import java.util.*;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GroundLevelingForgeEvents {
    private static Set<Block> enables;
    private static Set<Block> trees;
    private static Set<Block> leaves;
    private static Set<Block> ores;
    private static final HashMap<UUID, Integer> modeMap = new HashMap<>();
    private static final HashMap<UUID, Direction> clickedFace = new HashMap<>();
    private static final HashMap<UUID, Boolean> inProgressMap = new HashMap<>();
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (GroundLevelingKeyBindings.TOGGLE_DESTROY.consumeClick()) {
                //noinspection InstantiationOfUtilityClass
                GroundLevelingNetwork.CHANNEL.sendToServer(new GroundLevelingPacket());
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
                    BlockPos pos = task.found.poll();
                    if (pos == null) continue;
                    if (task.handler.destroyBlock(task, pos)) {
                        iterator.remove();
                        break;
                    }
                }

                if (task.found.isEmpty()) {
                    inProgressMap.put(task.player.getUUID(), false);
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player p = event.getPlayer();
        LazyOptional<IGroundLevelingData> cap = p.getCapability(GroundLevelingCapabilitySerializer.INSTANCE);
        cap.ifPresent(data -> modeMap.put(p.getUUID(), data.getMode()));
        if (modeMap.get(p.getUUID()) == 0 || inProgressMap.get(p.getUUID())) return;

        inProgressMap.put(p.getUUID(), true);
        Level l = p.level();
        if (l.isClientSide() || !(p instanceof ServerPlayer player) || !(l instanceof ServerLevel level) || p.isShiftKeyDown() || p.isCreative()) {
            inProgressMap.put(p.getUUID(), false);
            return;
        }

        BlockPos origin = event.getPos();

        if (!enables.contains(level.getBlockState(origin).getBlock())) {
            inProgressMap.put(player.getUUID(), false);
            return;
        }

        GroundLevelingBlockBreakEventHandler handler =
                new GroundLevelingBlockBreakEventHandler(enables, trees, leaves, ores);

        GroundLevelingTasks tasks =
                new GroundLevelingTasks(player, level, clickedFace.get(player.getUUID()), modeMap.get(player.getUUID()));

        handler.findBreakableBlocks(tasks, origin, event);
        tasks.handler = handler;

        ACTIVE_TASKS.add(tasks);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(GroundLeveling.MOD_ID, "breaker_mode"), new GroundLevelingCapabilitySerializer());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(o -> event.getEntity().getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(n -> n.setMode(o.getMode())));
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(data -> {
            int current = data.getMode();
            Player player = event.getEntity();
            if (current == 0) {
                player.sendSystemMessage(Component.translatable(TranslateKeys.MESSAGE_MODE_CHANGE_OFF));
            } else if (current == 1) {
                player.sendSystemMessage(Component.translatable(TranslateKeys.MESSAGE_MODE_CHANGE_MATERIAL_VEIN_MINING));
            } else {
                player.sendSystemMessage(Component.translatable(TranslateKeys.MESSAGE_MODE_CHANGE_GROUND_LEVELING));
            }
        });

        inProgressMap.put(event.getEntity().getUUID(), false);
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
}
