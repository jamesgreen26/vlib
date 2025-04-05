package g_mungus.vlib.util

import g_mungus.vlib.VLib.MAX_RECURSION
import kotlinx.coroutines.*
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import org.joml.Vector3i
import org.joml.Vector3ic
import org.joml.primitives.AABBi
import org.valkyrienskies.mod.common.util.toBlockPos
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage


val STRUCTURE_OFFSETS = listOf(
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

val defaultValidBlock: (BlockState) -> Boolean = {
    !it.isAir
}

fun findConnectedBlocks(
    level: Level,
    start: BlockPos,
): CompletionStage<Triple<Set<BlockPos>, Vector3ic, Vector3ic>> = CompletableFuture<Triple<Set<BlockPos>, Vector3ic, Vector3ic>>().also { future ->
    val result = mutableSetOf<BlockPos>()
    val visited = mutableSetOf<BlockPos>()
    val queue: ArrayDeque<BlockPos> = ArrayDeque()

    val min = Vector3i(Int.MAX_VALUE)
    val max = Vector3i(Int.MIN_VALUE)

    if (!defaultValidBlock(level.getBlockState(start))) future.complete(Triple(emptySet(), Vector3i(), Vector3i()))

    queue.add(start)
    visited.add(start)


    CoroutineScope(Dispatchers.Default).launch {
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            result.add(current)
            updateBounds(current, min, max)

            if (result.size > MAX_RECURSION) {
                future.completeExceptionally(RuntimeException("Max recursion reached"))
                break
            }

            for (offset in STRUCTURE_OFFSETS) {
                val neighbor = current.offset(offset)
                if (neighbor in visited) {
                    continue
                } else {
                    visited.add(neighbor)
                }

                val blockState = level.getBlockState(neighbor)

                if (blockState.`is`(Blocks.BEDROCK)) {
                    future.completeExceptionally(RuntimeException("Connected with bedrock"))
                    break
                } else if (defaultValidBlock(blockState)) {
                    queue.add(neighbor)
                }
            }
        }
        future.complete(Triple(result, min, max))
    }
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
