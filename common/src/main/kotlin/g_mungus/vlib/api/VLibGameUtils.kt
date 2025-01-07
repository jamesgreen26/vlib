package g_mungus.vlib.api

import g_mungus.vlib.VLib.LOGGER
import g_mungus.vlib.data.StructureSettings
import g_mungus.vlib.structure.StructureManager.enqueueTemplateForAssembly
import g_mungus.vlib.structure.TemplateAssemblyData
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import org.joml.Vector3d
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.shipObjectWorld


/**
 * Saves the ship found at the given (shipyard) BlockPos to a structure template, optionally deleting the ship after.
 *
 * @param structurePath The namespace or directory to save the structure template. Can either be in the format `namespace` or `namespace:folder/`
 * @param level The relevant ServerLevel
 * @param blockPos A block position in the shipyard on the ship to be saved
 * @param withEntities Whether to save non-player entities from the ship. I haven't tested it yet.
 * @param deleteAfter Whether to delete the ship after it has been saved to a structure template.
 *
 * @return A [Pair] with the [StructureTemplate] made and its saved-to [ResourceLocation]. Or null if no ship was found at the [blockPos]
 */
fun saveShipToTemplate(structurePath: String, level: ServerLevel, blockPos: BlockPos, withEntities: Boolean, deleteAfter: Boolean): Pair<StructureTemplate, ResourceLocation>? {
    val ship = level.getShipManagingPos(blockPos)
    if (ship != null) {
        return saveShipToTemplate(structurePath, level, ship, withEntities, deleteAfter)
    } else {
        LOGGER.warn("Could not find ship for blockPos: $blockPos in world: ${level.dimensionId}")
        return null
    }
}


/**
 * Saves the given ship to a structure template, optionally deleting the ship after.
 *
 * @param structurePath The namespace or directory to save the structure template. Can either be in the format <namespace> or <namespace:folder/>"
 * @param level The relevant ServerLevel
 * @param ship The ship to be saved
 * @param withEntities Whether to save non-player entities from the ship. I haven't tested it yet.
 * @param deleteAfter Whether to delete the ship after it has been saved to a structure template.
 *
 * @return A [Pair] with the [StructureTemplate] made and its saved-to [ResourceLocation]
 */
fun saveShipToTemplate(structurePath: String, level: ServerLevel, ship: Ship, withEntities: Boolean, deleteAfter: Boolean): Pair<StructureTemplate, ResourceLocation> {

    // Make a filename like 'ship_slug_goes_here' for later
    val filename = getShipFilename(ship)

    // Make a resource location from the structurePath
    val resourceLocation = if (structurePath.contains(':')) {
        ResourceLocation(structurePath + filename)
    } else if (structurePath.contains('/')) {
        throw IllegalArgumentException("Invalid structure path: the required format is either:\nnamespace\n- or -\nnamespace:folder/")
    } else {
        ResourceLocation(structurePath, filename)
    }

    // Make a new structure template at the resource location
    val structureTemplate = level.structureManager.getOrCreate(resourceLocation)

    // Save all ship blocks to the structure template
    structureTemplate.fillFromWorld(level, getMin(ship.shipAABB), getSize(ship.shipAABB), withEntities, Blocks.AIR)

    // Save the structure template back to the resource location
    level.structureManager.save(resourceLocation)

    // Delete the ship if we need to
    if (deleteAfter) { level.shipObjectWorld.deleteShip(ship as ServerShip) }

    return Pair(structureTemplate, resourceLocation)
}

/**
 * Returns a nice string name for a ship.
 * Will first try to use the ships slug,
 * then the ship's id if no slug was found.
 * @ship The ship to get the name of
 */
private fun getShipFilename(ship: Ship): String {
    return if (ship.slug != null) {
        ship.slug!!
    } else {
        "ship_num_" + ship.id
    }
}

private fun getMin(aabb: AABBic?): BlockPos {

    if (aabb != null) {
        return BlockPos(aabb.minX()-1, aabb.minY()-1, aabb.minZ()-1)
    } else {
        throw Exception("Why doesn't the ship have a shipyard AABB?")
    }
}

private fun getSize(aabb: AABBic?): Vec3i {
    if (aabb != null) {
        return Vec3i(aabb.maxX() - aabb.minX() + 2, aabb.maxY() - aabb.minY() + 2, aabb.maxZ() - aabb.minZ() + 2)
    } else {
        throw Exception("Why doesn't the ship have a shipyard AABB?")
    }
}


/**
 * Untested
 */
fun changeDimension(ship: Ship, serverLevel: ServerLevel, targetDimension: ResourceKey<Level>) {

    val structureTemplate = saveShipToTemplate(
        structurePath = "vlib:interdimensional/",
        level = serverLevel,
        ship = ship,
        withEntities = true,
        deleteAfter = true
    )

    val structureSettings = StructureSettings(
        folder = "",
        rename = true,
        static = false
    )

    val targetLevel = serverLevel.server.getLevel(targetDimension)?: return

    val shipPos = ship.shipAABB?.center(Vector3d())
    var shipDest = BlockPos(0, 0, 0)

    if (shipPos != null) {
        if (targetLevel.maxBuildHeight > (shipPos.y + ship.shipAABB!!.maxY() - ship.shipAABB!!.minY())) {
            shipDest = BlockPos(shipPos.x.toInt(), shipPos.y.toInt(), shipPos.z.toInt())
        } else {
            shipDest = BlockPos(shipPos.x.toInt(), targetLevel.maxBuildHeight - ship.shipAABB!!.maxY() + ship.shipAABB!!.minY(), shipPos.z.toInt())
        }
    }

    enqueueTemplateForAssembly(
        TemplateAssemblyData(
            template = structureTemplate.first,
            id = structureTemplate.second,
            level = targetLevel,
            pos = shipDest,
            structureSettings = structureSettings,
        )
    )

}

