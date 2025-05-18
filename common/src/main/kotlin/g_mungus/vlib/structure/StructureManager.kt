package g_mungus.vlib.structure

import g_mungus.vlib.VLib.LOGGER
import g_mungus.vlib.data.StructureSettings
import g_mungus.vlib.util.CanRemoveTemplate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.toBlockPos
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.yRange
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object StructureManager {
    @Volatile
    private var modifiedStructures = mapOf<String, StructureSettings>()

    val assemblyQueue: Queue<TemplateAssemblyData> = ConcurrentLinkedQueue()

    val blacklist: ConcurrentHashMap<BlockPos, Pair<Long, String>> = ConcurrentHashMap()

    const val READY = "vlib\$ready"
    const val DIRTY = "vlib\$dirty"

    fun addModifiedStructures(namespace: String, structureSettings: StructureSettings) {
        val modifiedStructures = this.modifiedStructures.toMutableMap()
        modifiedStructures[namespace] = structureSettings
        this.modifiedStructures = modifiedStructures
    }

    fun resetModifiedStructures() {
        modifiedStructures = mapOf()
    }

    fun getModifiedStructures(): Map<String, StructureSettings> {
        return this.modifiedStructures.toMap()
    }

    fun enqueueTemplateForAssembly(data: TemplateAssemblyData) {
        assemblyQueue.add(data)
        LOGGER.info("enqueueing template at ${data.pos}")
    }

    fun createShipFromTemplate(data: TemplateAssemblyData) {
        var pos = data.pos
        val now = Date().time
        val id = UUID.randomUUID().toString()

        if (blacklist.keys.contains(data.pos)) return
        blacklist[data.pos] = now to id

        if (data.level.isOutsideBuildHeight(data.pos)) {
            pos = BlockPos(pos.x, 0, pos.z)
        }

        if (blacklist[data.pos]?.second != id) return

        val ship = data.level.shipObjectWorld.createNewShipAtBlock(pos.toJOML(), false, 1.0, data.level.dimensionId)
        ship.isStatic = true
        val centreOfShip = ship.chunkClaim.getCenterBlockCoordinates(data.level.yRange, Vector3i()).toBlockPos().atY(pos.y)

        val structurePlaceSettings = StructurePlaceSettings()
        structurePlaceSettings.setRotationPivot(centreOfShip)

        data.template.placeInWorld(data.level, centreOfShip, centreOfShip, structurePlaceSettings, RandomSource.create(), 2)

        if (ship.inertiaData.mass < 0.001) {
            data.level.shipObjectWorld.deleteShip(ship)
            LOGGER.warn("Deleting ship with id: ${ship.id} because it has mass 0")
        } else {
            if (pos != data.pos) {
                data.level.shipObjectWorld.teleportShip(ship, ShipTeleportDataImpl(newPos = Vector3d(data.pos.x.toDouble(), data.pos.y.toDouble(), data.pos.z.toDouble())))
            }

            data.callback(ship, centreOfShip)
        }
        val manager: StructureTemplateManager = data.level.structureManager
        if ((manager as CanRemoveTemplate).`vlib$unloadTemplate`(data.template)) {
            LOGGER.info("Structure templates cleaned.")
        } else {
            LOGGER.error("Structure template cleanup failed")
        }
    }
}