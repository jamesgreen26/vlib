package g_mungus.vlib.mixin.v2.blockMass;

import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BitSetDiscreteVoxelShape.class)
public interface BitsetDiscreteVoxelShapeAccessor {

    @Invoker("forAllBoxes")
    static void forAllBoxes(DiscreteVoxelShape shape, DiscreteVoxelShape.IntLineConsumer consumer, boolean combine) {
        throw new AssertionError();
    }
}