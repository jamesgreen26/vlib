package g_mungus.vlib.mixin.massCalculator;

import g_mungus.vlib.util.CanAccessShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VoxelShape.class)
public class VoxelShapeAccessor implements CanAccessShape {

    @Final
    @Shadow
    protected DiscreteVoxelShape shape;

    @Unique
    public DiscreteVoxelShape vlib$getShape() {
        return shape;
    }
}
