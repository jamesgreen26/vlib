package g_mungus.vlib.api

import g_mungus.vlib.VLib.LOGGER
import g_mungus.vlib.VLib.MOD_ID
import g_mungus.vlib.structure.StructureManager
import g_mungus.vlib.util.CanFillByConnectivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import net.minecraft.world.phys.AABB
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.mod.common.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.random.Random


object VLibGameUtils {

    /**
     * Assembles the block and all connected blocks, within reasonable size.
     *
     * @return A completion stage which completes with the created ship when assembly is finished, or null if the blockPos specified was in the shipyard already.
     **/

    fun assembleByConnectivity(level: ServerLevel, blockPos: BlockPos): CompletionStage<Ship>? {
        if (level.isBlockInShipyard(blockPos)) return null

        val future = CompletableFuture<Ship>()
        val id = ResourceLocation(MOD_ID, "ships/" + Random.nextInt().toString())
        val template = level.structureManager.getOrCreate(id)

        (template as CanFillByConnectivity).`vlib$fillByConnectivity`(level, blockPos).whenComplete { t, u ->
            t?.let {
                template.author = StructureManager.READY + "%" + id
                template.placeInWorld(level, t.second, t.second, StructurePlaceSettings(), RandomSource.create(), 2)
                it.first.forEach { pos ->
                    val be = level.getBlockEntity(pos)
                    if (be != null) {
                        level.removeBlockEntity(pos)
                    }

                    level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 0)
                }
                CoroutineScope(Dispatchers.Default).launch {
                    var ship: Ship? = null
                    while(ship == null) {
                        delay(100)
                        ship = level.getShipsIntersecting(AABB(blockPos)).toList().firstOrNull()
                    }
                    delay(200)

                    it.first.forEach { pos ->
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)
                    }

                    (ship as ServerShip).isStatic = false
                    future.complete(ship)
                }
            }
            u?.printStackTrace()
        }
        return future
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
