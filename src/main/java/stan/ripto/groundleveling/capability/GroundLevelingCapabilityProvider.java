package stan.ripto.groundleveling.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroundLevelingCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final GroundLevelingData DATA = new GroundLevelingData();
    private final LazyOptional<IGroundLevelingData> OPTION = LazyOptional.of(() -> DATA);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GroundLevelingCapabilities.INSTANCE) {
            return OPTION.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        return (CompoundTag) DATA.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        DATA.deserializeNBT(nbt);
    }
}
