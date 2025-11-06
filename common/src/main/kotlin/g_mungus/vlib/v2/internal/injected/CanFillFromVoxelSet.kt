package g_mungus.vlib.v2.internal.injected

import g_mungus.vlib.v2.internal.assembly.BoundedVoxelSet
import net.minecraft.world.level.Level

interface CanFillFromVoxelSet {
    fun `vlib$fillFromVoxelSet`(level: Level, voxels: BoundedVoxelSet)
}