package g_mungus.vlib.v2.api.extension

import g_mungus.vlib.v2.impl.template.NamedStructureTemplate
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
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

fun Ship.getTemplateSize() = shipAABB!!.let { Vec3i(it.maxX() - it.minX() + 2, it.maxY() - it.minY() + 2, it.maxZ() - it.minZ() + 2) }
fun Ship.getTemplateCorner() = shipAABB!!.let { BlockPos(it.minX() - 1, it.minY() - 1, it.minZ() - 1) }

fun ServerShip.getTemplate(name: ResourceLocation, level: ServerLevel) =
    level.structureManager.getOrCreate(name).let {
        it.fillFromWorld(level, getTemplateCorner(), getTemplateSize(), false, Blocks.AIR)
        NamedStructureTemplate(it, name)
    }

fun ServerShip.saveToTemplate(name: ResourceLocation, level: ServerLevel) =
    getTemplate(name, level).also { level.structureManager.save(name) }

fun ServerShip.discard(level: ServerLevel) {
    level.shipObjectWorld.deleteShip(this)
}