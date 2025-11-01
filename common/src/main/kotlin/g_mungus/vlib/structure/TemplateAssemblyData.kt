package g_mungus.vlib.structure

import g_mungus.vlib.VLib
import g_mungus.vlib.api.HasSpecialSaveBehavior
import g_mungus.vlib.data.StructureSettings
import g_mungus.vlib.v2.util.forEachBlock
import g_mungus.vlib.v2.util.scheduleCallback
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import org.joml.Quaterniond
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl
import org.valkyrienskies.mod.common.shipObjectWorld
import kotlin.random.Random

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
        val rot = if (structureSettings.randomRotation) {
            val random = Random(pos.asLong())
            randomQuaternion(random)
        } else {
            Quaterniond()
        }

        val newPos: Vector3d = ship.transform.positionInWorld.add((ship as ServerShip).inertiaData.centerOfMassInShip, Vector3d()).sub(shipCenter.x.toDouble(), shipCenter.y.toDouble(), shipCenter.z.toDouble())

        level.shipObjectWorld.teleportShip(ship, ShipTeleportDataImpl(newPos = newPos, newRot = rot))

        ship.forEachBlock {
            val blockEntity = level.getBlockEntity(it)

            if (blockEntity is HasSpecialSaveBehavior) blockEntity.executeAfterLoadingShip()
        }

        level.scheduleCallback(4) {
            if (structureSettings.static != true) {
                ship.isStatic = false
            }
        }
    }


    private fun getName(location: String): String? {
        val i = location.lastIndexOf('/')
        return if (i >= 0) {
            location.substring(i + 1)
        } else location.ifBlank { null }
    }


    private fun randomQuaternion(random: Random): Quaterniond {
        val u1 = random.nextDouble()
        val u2 = random.nextDouble()
        val u3 = random.nextDouble()

        val sqrt1MinusU1 = kotlin.math.sqrt(1.0 - u1)
        val sqrtU1 = kotlin.math.sqrt(u1)

        val twoPiU2 = 2.0 * Math.PI * u2
        val twoPiU3 = 2.0 * Math.PI * u3

        val x = sqrt1MinusU1 * kotlin.math.sin(twoPiU2)
        val y = sqrt1MinusU1 * kotlin.math.cos(twoPiU2)
        val z = sqrtU1 * kotlin.math.sin(twoPiU3)
        val w = sqrtU1 * kotlin.math.cos(twoPiU3)

        return Quaterniond(x, y, z, w)
    }
}

