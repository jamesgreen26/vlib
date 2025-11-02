package g_mungus.vlib.v2.api

import g_mungus.vlib.VLib
import g_mungus.vlib.v2.api.extension.discard
import g_mungus.vlib.v2.api.extension.fillFromVoxelSet
import g_mungus.vlib.v2.api.extension.placeAsShip
import g_mungus.vlib.v2.api.extension.saveToTemplate
import g_mungus.vlib.v2.api.extension.scheduleCallback
import g_mungus.vlib.v2.api.extension.teleport
import g_mungus.vlib.v2.impl.assembly.BoundedVoxelSet
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.isBlockInShipyard
import org.valkyrienskies.mod.common.util.toBlockPos

object VLibAPI {

    @JvmStatic
    fun saveShipToTemplate(ship: ServerShip, resourceLocation: ResourceLocation, level: ServerLevel) = ship.saveToTemplate(resourceLocation, level)

    @JvmStatic
    fun discardShip(ship: ServerShip, level: ServerLevel) = ship.discard(level)

    @JvmStatic
    fun teleportShip(ship: ServerShip, level: ServerLevel, newPos: Vector3dc) = ship.teleport(level, newPos)

    @JvmStatic
    fun assembleByConnectivity(level: ServerLevel, start: BlockPos, blackList: List<Block> = listOf()): ServerShip? {
        if (level.isBlockInShipyard(start)) return null

        val voxelSet = BoundedVoxelSet.tryFillByConnectivity(level, start, blackList)?: return null

        val ship = StructureTemplate().let {
            it.fillFromVoxelSet(level, voxelSet)
            it.placeAsShip(level, voxelSet.min.toBlockPos(), true)
        } ?: return null

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
}