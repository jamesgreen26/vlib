package g_mungus.vlib.v2.internal.assembly

import g_mungus.vlib.VLib
import g_mungus.vlib.VLib.MAX_RECURSION
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3i
import org.joml.Vector3ic

data class BoundedVoxelSet(val voxels: Set<BlockPos>, val min: Vector3ic, val max: Vector3ic) {

    companion object {

        fun tryFillByConnectivity(level: Level, start: BlockPos, blackList: List<Block>): BoundedVoxelSet? {
            val combinedBlackList = mutableListOf<Block>().apply {
                addAll(defaultBlackList)
                addAll(blackList)
            }
            val result = mutableSetOf<BlockPos>()
            val visited = mutableSetOf<BlockPos>()
            val queue: ArrayDeque<BlockPos> = ArrayDeque()

            val min = Vector3i(Int.MAX_VALUE)
            val max = Vector3i(Int.MIN_VALUE)

            if (!isBlockStateValid(level.getBlockState(start), combinedBlackList)) return null

            queue.add(start)
            visited.add(start)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                result.add(current)
                updateBounds(current, min, max)

                if (result.size > MAX_RECURSION) return null

                for (offset in STRUCTURE_OFFSETS) {
                    val neighbor = current.offset(offset)
                    if (neighbor in visited) {
                        continue
                    } else {
                        visited.add(neighbor)
                    }

                    val blockState = level.getBlockState(neighbor)

                    if (blockState.`is`(Blocks.BEDROCK)) {
                        return null
                    } else if (isBlockStateValid(blockState, combinedBlackList)) {
                        queue.add(neighbor)
                    }
                }
            }
            return BoundedVoxelSet(result, min, max)

        }


        private fun updateBounds(current: BlockPos, min: Vector3i, max: Vector3i) {
            if (current.x < min.x) {
                min.x = current.x
            } else if (current.x > max.x) {
                max.x = current.x
            }
            if (current.y < min.y) {
                min.y = current.y
            } else if (current.y > max.y) {
                max.y = current.y
            }
            if (current.z < min.z) {
                min.z = current.z
            } else if (current.z > max.z) {
                max.z = current.z
            }
        }

        private fun isBlockStateValid(state: BlockState, blacklist: List<Block>) = blacklist.all { !state.`is`(it) }

        private val defaultBlackList = listOf(Blocks.AIR, Blocks.CAVE_AIR, VLib.GHOST_BLOCK)

        private val STRUCTURE_OFFSETS = listOf(
            // Face directions
            BlockPos(1, 0, 0),
            BlockPos(-1, 0, 0),
            BlockPos(0, 1, 0),
            BlockPos(0, -1, 0),
            BlockPos(0, 0, 1),
            BlockPos(0, 0, -1),

            // Edge directions (no corners)
            BlockPos(1, 1, 0),
            BlockPos(-1, 1, 0),
            BlockPos(1, -1, 0),
            BlockPos(-1, -1, 0),
            BlockPos(0, 1, 1),
            BlockPos(0, -1, 1),
            BlockPos(0, 1, -1),
            BlockPos(0, -1, -1),
            BlockPos(1, 0, 1),
            BlockPos(-1, 0, 1),
            BlockPos(1, 0, -1),
            BlockPos(-1, 0, -1)
        )
    }
}