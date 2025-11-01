package g_mungus.vlib.v2.util

import net.minecraft.core.BlockPos
import org.valkyrienskies.core.api.ships.Ship

fun Ship.forEachBlock(callback: (BlockPos) -> Unit) {
    val aabb = shipAABB?: return

    for (x in aabb.minX()..aabb.maxX()) {
        for (y in aabb.minY()..aabb.maxY()) {
            for (z in aabb.minZ()..aabb.maxZ()) {
                callback.invoke(BlockPos(x,y,z))
            }
        }
    }
}