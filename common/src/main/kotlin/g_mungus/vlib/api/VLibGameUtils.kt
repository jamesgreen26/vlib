package g_mungus.vlib.api

import g_mungus.vlib.VLib
import g_mungus.vlib.VLib.LOGGER
import g_mungus.vlib.v2.api.extension.fillFromVoxelSet
import g_mungus.vlib.v2.api.extension.placeAsShip
import g_mungus.vlib.v2.api.extension.scheduleCallback
import g_mungus.vlib.v2.impl.assembly.BoundedVoxelSet
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.mod.common.*
import org.valkyrienskies.mod.common.util.toBlockPos
import kotlin.random.Random

@Deprecated("move to VLib api v2")
object VLibGameUtils {

    /**
     * Assembles the block and all connected blocks, within reasonable size.
     *
     * @return A completion stage which completes with the created ship when assembly is finished, or null if the blockPos specified was in the shipyard already.
     **/

    fun assembleByConnectivity(level: ServerLevel, blockPos: BlockPos) =
        assembleByConnectivity(level, blockPos, listOf())

    fun assembleByConnectivity(level: ServerLevel, blockPos: BlockPos, blackList: List<Block> = listOf()): ServerShip? {
        if (level.isBlockInShipyard(blockPos)) return null

        val voxelSet = BoundedVoxelSet.tryFillByConnectivity(level, blockPos, blackList)?: return null

        val ship = StructureTemplate().let {
            it.fillFromVoxelSet(level, voxelSet)
            it.placeAsShip(level, voxelSet.min.toBlockPos(), true)
        }

        cleanupOriginalBlocks(level, voxelSet) {
            ship.isStatic = false
        }

        return ship
    }

    private fun cleanupOriginalBlocks(level: ServerLevel, voxelSet: BoundedVoxelSet, whenComplete: () -> Unit) {
        voxelSet.voxels.forEach { pos ->
            val be = level.getBlockEntity(pos)
            if (be != null) {
                level.removeBlockEntity(pos)
            }
            level.setBlock(pos, VLib.GHOST_BLOCK.defaultBlockState(), 0)
        }

        level.scheduleCallback(4) {
            voxelSet.voxels.forEach { pos ->
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)
            }

            whenComplete.invoke()
        }
    }

    /**
     * Saves the ship found at the given BlockPos to a structure template, optionally deleting the ship after.
     *
     * @param structurePath The namespace or directory to save the structure template. Can either be in the format <namespace> or <namespace:folder/>"
     * @param level The relevant ServerLevel
     * @param blockPos A block position in the shipyard on the ship to be saved
     * @param withEntities Whether to save non-player entities from the ship. I haven't tested it yet.
     * @param deleteAfter Whether to delete the ship after it has been saved to a structure template.
     *
     * @return nothing
     */
    fun saveShipToTemplate(
        structurePath: String,
        level: ServerLevel,
        blockPos: BlockPos,
        withEntities: Boolean,
        deleteAfter: Boolean
    ) {
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
     * @param withEntities Whether to save non-player entities from the ship. I haven't tested it yet.
     * @param deleteAfter Whether to delete the ship after it has been saved to a structure template.
     *
     * @return nothing
     */
    fun saveShipToTemplate(
        structurePath: String,
        level: ServerLevel,
        shipId: Long,
        withEntities: Boolean,
        deleteAfter: Boolean
    ) {
        val ship = level.allShips.getById(shipId) ?: let {
            LOGGER.error("Could not find ship id: $shipId in world: ${level.dimensionId}")
            return
        }

        val structureTemplateManager = level.structureManager
        structureTemplateManager.save(
            getStructureTemplate(
                structurePath,
                level,
                ship,
                withEntities,
                structureTemplateManager
            ).second
        )

        if (deleteAfter) {
            level.shipObjectWorld.deleteShip(ship as ServerShip)
        }
    }


    fun saveShipToTemplate2(
        structurePath: String,
        level: ServerLevel,
        blockPos: BlockPos,
        withEntities: Boolean,
        deleteAfter: Boolean
    ): Pair<ResourceLocation, BlockPos>? {

        val ship = level.getShipManagingPos(blockPos) ?: let {
            LOGGER.error("Could not find ship at pos: $blockPos in world: ${level.dimensionId}")
            return null
        }

        val min = getMin(ship.shipAABB)

        var resourceLocation: ResourceLocation

        val structureTemplateManager = level.structureManager
        structureTemplateManager.save(
            getStructureTemplate(
                structurePath,
                level,
                ship,
                withEntities,
                structureTemplateManager
            ).second.also { resourceLocation = it }
        )

        if (deleteAfter) {
            level.shipObjectWorld.deleteShip(ship)
        }
        return resourceLocation to min
    }

    /**
     * Creates a structure template to represent the provided ship, without saving it to disk. Useful if you want to use the template immediately and discard it afterward.
     *
     * @return a pair of the structure template and the resource location where it would be saved.
     **/

    fun getStructureTemplate(
        structurePath: String,
        level: ServerLevel,
        ship: Ship,
        withEntities: Boolean,
        structureTemplateManager: StructureTemplateManager
    ): Pair<StructureTemplate, ResourceLocation> {
        val shipName: String = ship.slug ?: "ship_${Random.Default.nextInt()}"

        val resourceLocation = if (structurePath.contains(':')) {
            ResourceLocation(structurePath + shipName)
        } else if (structurePath.contains('/')) {
            throw IllegalArgumentException("Invalid structure path: the required format is either:\nnamespace\n- or -\nnamespace:folder/")
        } else {
            ResourceLocation(structurePath, shipName)
        }

        val structureTemplate = structureTemplateManager.getOrCreate(resourceLocation)

        structureTemplate.fillFromWorld(level, getMin(ship.shipAABB), getSize(ship.shipAABB), withEntities, Blocks.AIR)


        return Pair(structureTemplate, resourceLocation)
    }

    private fun getMin(aabb: AABBic?): BlockPos {

        if (aabb != null) {
            return BlockPos(aabb.minX() - 1, aabb.minY() - 1, aabb.minZ() - 1)
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
}
