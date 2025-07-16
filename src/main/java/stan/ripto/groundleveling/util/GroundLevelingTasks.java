package stan.ripto.groundleveling.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class GroundLevelingTasks {
    public final ServerPlayer player;
    public final ServerLevel level;
    public final Queue<BlockPos> queue = new ArrayDeque<>();
    public final Queue<BlockPos> found = new ArrayDeque<>();
    public final List<BlockPos> visited = new ArrayList<>();
    public final Direction face;
    public final int mode;

    public GroundLevelingBlockBreakEventHandler handler;

    public GroundLevelingTasks(ServerPlayer player, ServerLevel level, Direction face, int mode) {
        this.player = player;
        this.level = level;
        this.face = face;
        this.mode = mode;
    }

    public void foundCopy() {
        this.found.addAll(this.visited);
    }
}
