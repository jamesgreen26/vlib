package g_mungus.vlib.mixin.shipPlacement;

import g_mungus.vlib.data.StructureSettings;
import g_mungus.vlib.structure.StructureManager;
import g_mungus.vlib.v2.internal.template.StructureTemplateInternalExtKt;
import g_mungus.vlib.v2.internal.template.VLibStructureData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(value = StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {

    @Final
    @Shadow
    private Map<ResourceLocation, Optional<StructureTemplate>> structureRepository;

    @Shadow
    protected abstract Optional<StructureTemplate> tryLoad(ResourceLocation identifier);


    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void getTemplateMixin(ResourceLocation id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {

        Optional<StructureTemplate> template = this.structureRepository.computeIfAbsent(id, this::tryLoad);

        if (template.isPresent() && StructureManager.INSTANCE.getModifiedStructures().containsKey(id.getNamespace())) {
            StructureSettings structureSettings = StructureManager.INSTANCE.getModifiedStructures().get(id.getNamespace());
            boolean shouldAssemble = false;
            for (String folder : structureSettings.getFolders()) {
                if (id.getPath().startsWith(folder)) {
                    shouldAssemble = true;
                    break;
                }
            }

            if (shouldAssemble) {
                StructureTemplateInternalExtKt.saveAdditional(template.get(), new VLibStructureData(true));
            }
        }
        cir.setReturnValue(template);
    }
}
