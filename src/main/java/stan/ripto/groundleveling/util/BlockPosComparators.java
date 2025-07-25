package stan.ripto.groundleveling.util;

import net.minecraft.core.BlockPos;

import java.util.Comparator;

public class BlockPosComparators {
    private final GroundLevelingTasks task;
    private final BlockPos origin;

    public final Comparator<BlockPos> COM = new Comparator<>() {
        @Override
        public int compare(BlockPos o1, BlockPos o2) {
            int dif1 = 0, dif2 = 0;
            switch (task.face) {
                case EAST, WEST -> {
                    dif1 = Math.abs(origin.getX() - o1.getX());
                    dif2 = Math.abs(origin.getX() - o2.getX());
                }
                case NORTH, SOUTH -> {
                    dif1 = Math.abs(origin.getZ() - o1.getZ());
                    dif2 = Math.abs(origin.getZ() - o2.getZ());
                }
            }
            return Integer.compare(dif1, dif2);
        }
    };

    public BlockPosComparators(GroundLevelingTasks task, BlockPos origin) {
        this.task = task;
        this.origin = origin;
    }
}
