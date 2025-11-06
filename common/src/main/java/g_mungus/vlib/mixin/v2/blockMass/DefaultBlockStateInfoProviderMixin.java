package g_mungus.vlib.mixin.v2.blockMass;

import g_mungus.vlib.v2.internal.CalculateBlockMassKt;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.DefaultBlockStateInfoProvider;

@Mixin(value = DefaultBlockStateInfoProvider.class, remap = false)
public class DefaultBlockStateInfoProviderMixin {

    @Inject(method = "getBlockStateMass", at = @At("HEAD"), cancellable = true)
    void getBlockStateMass(BlockState blockState, CallbackInfoReturnable<Double> cir) {
        if (blockState.isAir()) {
            cir.setReturnValue(0.0);
        } else {
            cir.setReturnValue(CalculateBlockMassKt.calculateBlockMass(blockState));
        }
    }
}
