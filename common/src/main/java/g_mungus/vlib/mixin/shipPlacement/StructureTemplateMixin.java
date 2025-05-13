package g_mungus.vlib.mixin.shipPlacement;

import g_mungus.vlib.data.StructureSettings;
import g_mungus.vlib.structure.StructureManager;
import g_mungus.vlib.structure.TemplateAssemblyData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Objects;

@Mixin(value = StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Shadow private String author;

    @Shadow
    public abstract void setAuthor(String author);

    @Inject(method = "placeInWorld", at = @At("HEAD"), cancellable = true)
    public void placeMixin(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int i, CallbackInfoReturnable<Boolean> cir) {
        if (serverLevelAccessor.isClientSide()) return;

        if (VSGameUtilsKt.isBlockInShipyard(serverLevelAccessor.getLevel(), blockPos)) return;

        if (this.author.startsWith(StructureManager.READY)) {

            ResourceLocation id = new ResourceLocation(Objects.requireNonNull(vlib$getNameSpace(this.author, '%')));

            StructureSettings structureSettings = StructureManager.INSTANCE.getModifiedStructures().get(id.getNamespace());

            TemplateAssemblyData data = new TemplateAssemblyData(
                (StructureTemplate) (Object) this,
                id,
                serverLevelAccessor.getLevel(),
                blockPos,
                structureSettings
            );

            StructureManager.INSTANCE.enqueueTemplateForAssembly(data);

            this.setAuthor(StructureManager.DIRTY);
            cir.setReturnValue(true);
        } else if (this.author.equals(StructureManager.DIRTY)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static String vlib$getNameSpace(String location, char separator) {
        int i = location.indexOf(separator);
        if (i >= 0) {
            return location.substring(i + 1);
        }

        return null;
    }
}
