package g_mungus.vlib.api

import g_mungus.vlib.VLib.LOGGER
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.mod.common.*
import kotlin.random.Random

/**
 * Saves the ship found at the given BlockPos to a structure template, optionally deleting the ship after.
 *
 * @param structurePath The namespace or directory to save the structure template. Can either be in the format <namespace> or <namespace:folder/>"
 * @param level The relevant ServerLevel
 * @param blockPos A block position in the shipyard on the ship to be saved
 * @param withEntities Whether to save non-player entities from the ship. I don't think it works right now.
 * @param deleteAfter Whether to delete the ship after it has been saved to a structure template.
 *
 * @return nothing
 */
fun saveShipToTemplate(structurePath: String, level: ServerLevel, blockPos: BlockPos, withEntities: Boolean, deleteAfter: Boolean) {
    val shipID = level.getShipManagingPos(blockPos)?.id
    if (shipID != null) {
        saveShipToTemplate(structurePath, level, shipID, withEntities, deleteAfter)
    } else {
        LOGGER.error("Could not find ship for blockPos: $blockPos in world: ${level.dimensionId}")
    }
}

/**
 * Saves the ship of the given ship ID to a structure template, optionally deleting the ship after.
 *
 * @param structurePath The namespace or directory to save the structure template. Can either be in the format <namespace> or <namespace:folder/>"
 * @param level The relevant ServerLevel
 * @param shipId The shipID of the ship to be saved
 * @param withEntities Whether to save non-player entities from the ship. I don't think it works right now.
 * @param deleteAfter Whether to delete the ship after it has been saved to a structure template.
 *
 * @return nothing
 */
fun saveShipToTemplate(structurePath: String, level: ServerLevel, shipId: Long, withEntities: Boolean, deleteAfter: Boolean) {
    val ship = level.allShips.getById(shipId)
    if (ship == null) {
        LOGGER.error("Could not find ship id: $shipId in world: ${level.dimensionId}")
        return
    }

    val structureTemplateManager = level.structureManager


    structureTemplateManager.save(getStructureTemplate(structurePath, level, ship, withEntities, structureTemplateManager).second)

    if (deleteAfter) {
        level.shipObjectWorld.deleteShip(ship as ServerShip)
    }

}

private fun getStructureTemplate (structurePath: String, level: ServerLevel, ship: Ship, withEntities: Boolean, structureTemplateManager: StructureTemplateManager): Pair<StructureTemplate, ResourceLocation> {

    val shipName: String = if (ship.slug != null) {
        ship.slug!!
    } else {
        "ship_" + Random.Default.nextInt().toString()
    }

    val structureTemplate = structureTemplateManager.getOrCreate(ResourceLocation(structurePath, shipName))

    structureTemplate.fillFromWorld(level, getMin(ship.shipAABB), getSize(ship.shipAABB), withEntities, Blocks.AIR)


    return if (structurePath.contains(':')) {
        Pair(structureTemplate, ResourceLocation(structurePath + shipName))
    } else if (structurePath.contains('/')) {
        throw IllegalArgumentException("Invalid structure path: the required format is either:\nnamespace\n- or -\nnamespace:folder/")
    } else {
        Pair(structureTemplate, ResourceLocation(structurePath, shipName))
    }
}

private fun getMin(aabb: AABBic?): BlockPos {

    if (aabb != null) {
        return BlockPos(aabb.minX(), aabb.minY(), aabb.minZ())
    } else {
        throw Exception("Why doesn't the ship have a shipyard AABB?")
    }
}

private fun getSize(aabb: AABBic?): Vec3i {
    if (aabb != null) {
        return Vec3i(aabb.maxX() - aabb.minX(), aabb.maxY() - aabb.minY(), aabb.maxZ() - aabb.minZ())
    } else {
        throw Exception("Why doesn't the ship have a shipyard AABB?")
    }
}

