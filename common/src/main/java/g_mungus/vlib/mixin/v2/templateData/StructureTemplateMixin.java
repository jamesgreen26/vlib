package g_mungus.vlib.mixin.v2.templateData;

import g_mungus.vlib.v2.impl.template.StructureTemplateExtKt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Inject(method = "placeInWorld", at = @At("HEAD"), cancellable = true)
    void redirectToShipPlacement(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int i, CallbackInfoReturnable<Boolean> cir) {
        if (vlib$isInShipyard(blockPos)) { return; }

        StructureTemplate casted = (StructureTemplate) (Object) this;
        if (StructureTemplateExtKt.readAdditional(casted).isShip() && serverLevelAccessor instanceof ServerLevel serverLevel) {
            g_mungus.vlib.v2.api.extension.StructureTemplateExtKt.placeAsShip(casted, serverLevel, blockPos, false);
            cir.setReturnValue(true);
        }
    }

    @Unique
    boolean vlib$isInShipyard(BlockPos pos) {
        return pos.getX() >= -28_672_000 && pos.getX() <= 28_672_000 &&
                pos.getZ() >= 12_288_000 && pos.getZ() <= 28_672_000;
    }
}
