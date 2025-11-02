package g_mungus.vlib.v2.util.injected

import g_mungus.vlib.v2.impl.assembly.BoundedVoxelSet
import net.minecraft.world.level.Level

interface CanFillFromVoxelSet {
    fun `vlib$fillFromVoxelSet`(level: Level, voxels: BoundedVoxelSet)
}