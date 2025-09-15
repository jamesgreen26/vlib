package g_mungus.vlib.structure

import g_mungus.vlib.VLib
import g_mungus.vlib.data.StructureSettings
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl
import org.valkyrienskies.mod.common.shipObjectWorld

class TemplateAssemblyData (
    val template: StructureTemplate,
    val id: ResourceLocation,
    val level: ServerLevel,
    val pos: BlockPos,
    val structureSettings: StructureSettings
) {
    fun callback(ship: Ship, shipCenter: BlockPos) {
        if (structureSettings.rename == true) {
            val name = getName(id.path)
            if (name != null) {
                (ship as ServerShip).slug = name
            } else {
                VLib.LOGGER.warn("Failed to rename ship")
            }
        }
        val newPos: Vector3d = ship.transform.positionInWorld.add((ship as ServerShip).inertiaData.centerOfMassInShip, Vector3d()).sub(shipCenter.x.toDouble(), shipCenter.y.toDouble(), shipCenter.z.toDouble())

        level.shipObjectWorld.teleportShip(ship, ShipTeleportDataImpl(newPos = newPos))

        if (structureSettings.static != true) {
            ship.isStatic = false
        }
    }


    private fun getName(location: String): String? {
        val i = location.lastIndexOf('/')
        return if (i >= 0) {
            location.substring(i + 1)
        } else location.ifBlank { null }
    }
}

