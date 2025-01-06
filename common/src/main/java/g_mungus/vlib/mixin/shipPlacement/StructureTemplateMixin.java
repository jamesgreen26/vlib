package g_mungus.vlib.mixin.shipPlacement;

import g_mungus.vlib.structure.StructureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(value = StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Shadow private String author;

    @Shadow
    public abstract void setAuthor(String author);

    @Inject(method = "placeInWorld", at = @At("HEAD"), cancellable = true)
    public void placeMixin(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int i, CallbackInfoReturnable<Boolean> cir) {
        if (VSGameUtilsKt.isBlockInShipyard(serverLevelAccessor.getLevel(), blockPos)) return;

        if (this.author.equals(StructureManager.READY)) {
            StructureManager.INSTANCE.enqueueTemplateForAssembly(
                    (StructureTemplate) (Object) this,
                    serverLevelAccessor.getLevel(),
                    blockPos);

            this.setAuthor(StructureManager.DIRTY);
            cir.setReturnValue(true);
        } else if (this.author.equals(StructureManager.DIRTY)) {
            cir.setReturnValue(false);
        }
    }
}
