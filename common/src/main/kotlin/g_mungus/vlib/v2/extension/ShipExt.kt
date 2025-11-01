package g_mungus.vlib.v2.extension

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import org.joml.Quaterniond
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl
import org.valkyrienskies.mod.common.shipObjectWorld

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

fun ServerShip.teleport(level: ServerLevel, newPos: Vector3dc) {
    level.shipObjectWorld.teleportShip(this, ShipTeleportDataImpl(newPos = newPos, newRot = this.worldToShip.getNormalizedRotation(Quaterniond())))
}