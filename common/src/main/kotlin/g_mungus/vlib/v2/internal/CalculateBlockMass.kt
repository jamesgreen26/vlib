package g_mungus.vlib.v2.internal

import g_mungus.vlib.mixin.v2.blockMass.BitsetDiscreteVoxelShapeAccessor
import g_mungus.vlib.mixin.v2.blockMass.VoxelShapeAccessor
import net.minecraft.core.BlockPos
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import kotlin.math.max
import kotlin.math.roundToInt

const val DEFAULT_DENSITY = 1000.0
const val MIN_MASS = 10.0

fun calculateBlockMass(blockState: BlockState): Double {
    val voxelShape = blockState.block.getShape(
        blockState,
        EmptyBlockGetter.INSTANCE,
        BlockPos(0, 0, 0),
        CollisionContext.empty()
    )

    val shape = (voxelShape as VoxelShapeAccessor).shape
    var volume = 0.0

    BitsetDiscreteVoxelShapeAccessor.forAllBoxes(
        shape,
        { a, b, c, d, e, f ->
            val xSize = (d - a) / shape.xSize.toDouble()
            val ySize = (e - b) / shape.ySize.toDouble()
            val zSize = (f - c) / shape.zSize.toDouble()
            volume += xSize * ySize * zSize
        }, false
    )

    return max((volume * DEFAULT_DENSITY * 10.0).roundToInt() / 10.0, MIN_MASS)
}