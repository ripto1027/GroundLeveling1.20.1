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
import stan.ripto.groundleveling.datagen.lang.TranslateKeys;
import stan.ripto.groundleveling.key.GroundLevelingKeyBindings;
import stan.ripto.groundleveling.network.GroundLevelingNetwork;
import stan.ripto.groundleveling.network.GroundLevelingPacket;

import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = GroundLeveling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GroundLevelingForgeEvents {
    private static Set<Block> enables;
    private static Set<Block> trees;
    private static Set<Block> leaves;
    private static Set<Block> ores;
    private static final WeakHashMap<UUID, Direction> clickedFace = new WeakHashMap<>();
    private static int mode;
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (GroundLevelingKeyBindings.TOGGLE_DESTROY.consumeClick()) {
                //noinspection InstantiationOfUtilityClass
                GroundLevelingNetwork.CHANNEL.sendToServer(new GroundLevelingPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player p = event.getPlayer();
        p.getCapability(GroundLevelingCapabilitySerializer.INSTANCE).ifPresent(data -> mode = data.getMode());
        if (mode == 0 || isInProgress) return;

        isInProgress = true;
        Level l = p.level();
        if (l.isClientSide() || !(p instanceof ServerPlayer player) || !(l instanceof ServerLevel level) || p.isShiftKeyDown()) {
            isInProgress = false;
            return;
        }

        Direction face = clickedFace.get(p.getUUID());
        BlockPos origin = event.getPos();
        Block originBlock = level.getBlockState(origin).getBlock();
        if (!enables.contains(originBlock)) {
            isInProgress = false;
            return;
        }

        GroundLevelingBlockBreakEventHelper helper =
                new GroundLevelingBlockBreakEventHelper(enables, trees, leaves, ores, face);

        if (mode == 1) {
            if (trees.contains(originBlock)) {
                helper.treeBreaker(level, origin, player);
            } else if (ores.contains(originBlock)) {
                helper.oreBreaker(level, origin, player);
            } else {
                isInProgress = false;
                return;
            }
        } else {
            if (face == Direction.DOWN || face == Direction.UP) {
                isInProgress = false;
                return;
            } else {
                helper.rangeBreaker(level, origin, player);
            }
        }

        isInProgress = false;
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
