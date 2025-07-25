package stan.ripto.groundleveling.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import stan.ripto.groundleveling.GroundLeveling;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilities;
import stan.ripto.groundleveling.capability.GroundLevelingCapabilityProvider;
import stan.ripto.groundleveling.capability.GroundLevelingData;
import stan.ripto.groundleveling.command.GroundLevelingConfigLoadCommand;
import stan.ripto.groundleveling.config.GroundLevelingConfigs;
import stan.ripto.groundleveling.datagen.lang.TranslateKeys;
import stan.ripto.groundleveling.key.GroundLevelingKeyMappings;
import stan.ripto.groundleveling.network.GroundLevelingInProcessingChangePacket;
import stan.ripto.groundleveling.network.GroundLevelingModeCheckPacket;
import stan.ripto.groundleveling.network.GroundLevelingNetwork;
import stan.ripto.groundleveling.network.GroundLevelingModeChangePacket;
import stan.ripto.groundleveling.util.GroundLevelingBlockBreakEventHandler;
import stan.ripto.groundleveling.util.GroundLevelingConfigLoadHandler;
import stan.ripto.groundleveling.util.GroundLevelingTasks;

import java.util.*;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GroundLevelingForgeEvents {
    private static final List<GroundLevelingTasks> ACTIVE_TASKS = new ArrayList<>();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GroundLevelingConfigLoadCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        GroundLevelingConfigLoadHandler.load();
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (GroundLevelingKeyMappings.CHANGE_MODE.consumeClick()) {
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
                int blockPerTick = GroundLevelingConfigs.SERVER.BREAK_PER_TICK.get();

                for (int i = 0; i < blockPerTick; i++) {
                    if (task.found.isEmpty()) {
                        GroundLevelingNetwork.CHANNEL.sendToServer(new GroundLevelingInProcessingChangePacket(false));
                        iterator.remove();
                        break;
                    }

                    BlockPos pos = task.found.poll();
                    if (!task.handler.destroyBlock(task, pos)) {
                        GroundLevelingNetwork.CHANNEL.sendToServer(new GroundLevelingInProcessingChangePacket(false));
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

        p.getCapability(GroundLevelingCapabilities.INSTANCE).ifPresent(data -> {
            int m = data.getMode();

            if (m == 0 || data.isInProcessing() || p.isShiftKeyDown() || p.isCreative()) return;


            BlockHitResult hit = (BlockHitResult) p.pick(5.0D, 1.0F, false);
            Direction face = hit.getDirection();
            if (m == 2 && (face == Direction.UP || face == Direction.DOWN)) return;

            Level l = p.level();
            if (!(p instanceof ServerPlayer player) || !(l instanceof ServerLevel level)) return;

            BlockPos origin = event.getPos();
            Block originBlock = level.getBlockState(origin).getBlock();

            int findType;
            if (m == 1) {
                if (GroundLevelingConfigLoadHandler.TREES.contains(originBlock)) {
                    findType = 0;
                } else if (GroundLevelingConfigLoadHandler.GRASSES.contains(originBlock)) {
                    findType = 1;
                } else if (!GroundLevelingConfigLoadHandler.BLACKLIST.contains(originBlock)) {
                    findType = 2;
                } else {
                    return;
                }
            } else {
                if (!GroundLevelingConfigLoadHandler.DISABLES.contains(originBlock)) {
                    findType = 3;
                } else {
                    return;
                }
            }

            data.setInProcessing(true);

            GroundLevelingBlockBreakEventHandler handler =
                    new GroundLevelingBlockBreakEventHandler();

            GroundLevelingTasks task =
                    new GroundLevelingTasks(player, level, face, findType);

            handler.findBreakableBlocks(task, origin, event);
            task.handler = handler;

            ACTIVE_TASKS.add(task);
        });
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    ResourceLocation.fromNamespaceAndPath(GroundLeveling.MOD_ID, "breaker_mode"),
                    new GroundLevelingCapabilityProvider()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag originalTag = event.getOriginal().serializeNBT();

        if (originalTag.contains("ForgeCaps")) {
            CompoundTag forgeCaps = originalTag.getCompound("ForgeCaps");

            ResourceLocation capKey =
                    ResourceLocation.fromNamespaceAndPath(GroundLeveling.MOD_ID, "breaker_mode");

            String key = capKey.toString();

            if (forgeCaps.contains(key)) {
                CompoundTag targetTag = forgeCaps.getCompound(key);

                event.getEntity().getCapability(GroundLevelingCapabilities.INSTANCE)
                        .ifPresent(serverData -> {
                            int currentMode = targetTag.getInt(GroundLevelingData.MODE_KEY);
                            serverData.setMode(currentMode);
                        });
            }
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (ACTIVE_TASKS.isEmpty()) return;

        UUID uuid = event.getEntity().getUUID();
        ACTIVE_TASKS.removeIf(task -> task.player.getUUID().equals(uuid));
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        player.getCapability(GroundLevelingCapabilities.INSTANCE).ifPresent(data -> {
            if (!data.isSynced()) {
                GroundLevelingNetwork.CHANNEL.sendToServer(new GroundLevelingModeCheckPacket());
            }

            int m = data.getMode();

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
        });
    }
}
