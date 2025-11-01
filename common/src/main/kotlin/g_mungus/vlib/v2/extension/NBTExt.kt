package g_mungus.vlib.v2.extension

import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag

fun CompoundTag.putVec3i(prefix: String, vec: Vec3i) =
    with(vec) {
        putInt(prefix + "x", x)
        putInt(prefix + "y", y)
        putInt(prefix + "z", z)
    }

fun CompoundTag.getVec3i(prefix: String): Vec3i? {
    return if (
        !this.contains(prefix + "x") ||
        !this.contains(prefix + "y") ||
        !this.contains(prefix + "z")
    ) {
        null
    } else {
        Vec3i(
            this.getInt(prefix + "x"),
            this.getInt(prefix + "y"),
            this.getInt(prefix + "z")
        )
    }
}
