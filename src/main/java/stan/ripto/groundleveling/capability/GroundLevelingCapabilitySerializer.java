package stan.ripto.groundleveling.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroundLevelingCapabilitySerializer implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<IGroundLevelingData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    public final GroundLevelingData BREAKER_MODE = new GroundLevelingData();
    private final LazyOptional<IGroundLevelingData> OPTION = LazyOptional.of(() -> BREAKER_MODE);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == INSTANCE) {
            return OPTION.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("breaker_mode", BREAKER_MODE.getMode());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        BREAKER_MODE.setMode(nbt.getInt("breaker_mode"));
    }
}
