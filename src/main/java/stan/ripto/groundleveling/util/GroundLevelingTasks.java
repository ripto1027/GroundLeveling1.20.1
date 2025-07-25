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
    public final Set<BlockPos> visited = new HashSet<>();
    public final List<BlockPos> willSortList = new ArrayList<>();
    public final Direction face;
    public final int findType;

    public GroundLevelingBlockBreakEventHandler handler;

    public GroundLevelingTasks(ServerPlayer player, ServerLevel level, Direction face, int findType) {
        this.player = player;
        this.level = level;
        this.face = face;
        this.findType = findType;
    }
}
