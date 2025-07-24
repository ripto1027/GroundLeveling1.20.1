package stan.ripto.groundleveling.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class GroundLevelingCapabilities {
    public static final Capability<IGroundLevelingData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
}
