package stan.ripto.groundleveling.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public class GroundLevelingData implements IGroundLevelingData, INBTSerializable<Tag> {
    public static final String MODE_KEY = "GroundLevelingMode";
    public static final String SYNCED_KEY = "GroundLevelingSynced";
    private int mode = 0;
    private boolean synced = false;
    private boolean inProcessing = false;

    @Override
    public int getMode() {
        return this.mode;
    }

    @Override
    public void changeMode() {
        if (this.mode == 2) {
            this.mode = 0;
        } else {
            this.mode++;
        }
    }

    @Override
    public void setMode(int value) {
        if (0 <= value && value <= 2) {
            this.mode = value;
        } else {
            this.mode = 0;
        }
    }

    @Override
    public boolean isSynced() {
        return this.synced;
    }

    @Override
    public void setSynced(boolean value) {
        this.synced = value;
    }

    @Override
    public boolean isInProcessing() {
        return this.inProcessing;
    }

    @Override
    public void setInProcessing(boolean value) {
        this.inProcessing = value;
    }

    @Override
    public Tag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(MODE_KEY, this.mode);
        nbt.putBoolean(SYNCED_KEY, this.synced);
        return nbt;
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (tag instanceof CompoundTag nbt) {
            this.mode = nbt.getInt(MODE_KEY);
            this.synced = nbt.getBoolean(SYNCED_KEY);
        }
    }
}
