package g_mungus.vlib.util;

import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface CanFillByConnectivity {
    CompletionStage<Pair<Set<BlockPos>, BlockPos>> vlib$fillByConnectivity(Level level, BlockPos pos);
}
