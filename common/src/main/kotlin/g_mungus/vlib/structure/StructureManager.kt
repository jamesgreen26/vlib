package g_mungus.vlib.structure

import g_mungus.vlib.VLib.LOGGER
import g_mungus.vlib.data.StructureSettings
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import org.joml.Vector3i
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.toBlockPos
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.yRange
import java.util.*
import kotlin.collections.ArrayDeque

object StructureManager {
    @Volatile
    private var modifiedStructures = mapOf<String, StructureSettings>()

    val assemblyQueue: Queue<Triple<StructureTemplate, ServerLevel, BlockPos>> = java.util.ArrayDeque()

    const val READY = "vlib\$ready"
    const val DIRTY = "vlib\$dirty"

    fun addModifiedStructures(namespace: String, structureSettings: StructureSettings) {
        val modifiedStructures = this.modifiedStructures.toMap().toMutableMap()
        modifiedStructures[namespace] = structureSettings
        this.modifiedStructures = modifiedStructures
    }

    fun resetModifiedStructures() {
        modifiedStructures = mapOf()
    }

    fun getModifiedStructures(): Map<String, StructureSettings> {
        return this.modifiedStructures.toMap()
    }

    fun enqueueTemplateForAssembly(structureTemplate: StructureTemplate, serverLevel: ServerLevel, blockPos: BlockPos) {
        assemblyQueue.add(Triple(structureTemplate, serverLevel, blockPos))
        LOGGER.info("enqueueing template at $blockPos")
    }

    fun createShipFromTemplate(structureTemplate: StructureTemplate, serverLevel: ServerLevel, blockPos: BlockPos) {
        val ship = serverLevel.shipObjectWorld.createNewShipAtBlock(blockPos.toJOML(), false, 1.0, serverLevel.dimensionId)
        ship.isStatic = true
        val centreOfShip = ship.chunkClaim.getCenterBlockCoordinates(serverLevel.yRange, Vector3i()).toBlockPos().atY(blockPos.y)

        val structurePlaceSettings = StructurePlaceSettings()
        structurePlaceSettings.setRotationPivot(centreOfShip)

        val successfullyPlaced = structureTemplate.placeInWorld(serverLevel, centreOfShip, centreOfShip, structurePlaceSettings, RandomSource.create(), 2)

        if (ship.inertiaData.mass < 0.001) {
            serverLevel.shipObjectWorld.deleteShip(ship)
            LOGGER.warn("Deleting ship with id: ${ship.id} because it has mass 0")
        } else {
            ship.isStatic = false
        }
    }
}