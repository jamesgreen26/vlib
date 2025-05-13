package g_mungus.vlib.mixin.shipPlacement;


import g_mungus.vlib.VLib;
import g_mungus.vlib.data.StructureSettings;
import g_mungus.vlib.util.CanRemoveTemplate;
import g_mungus.vlib.structure.StructureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(value = StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin implements CanRemoveTemplate {

    @Final
    @Shadow
    private Map<ResourceLocation, Optional<StructureTemplate>> structureRepository;

    @Shadow
    protected abstract Optional<StructureTemplate> tryLoad(ResourceLocation identifier);

    @Shadow public abstract void remove(ResourceLocation id);


    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void getTemplateMixin(ResourceLocation id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {

        Optional<StructureTemplate> template = this.structureRepository.computeIfAbsent(id, this::tryLoad);

        if (template.isPresent() && !template.get().getAuthor().equals(StructureManager.DIRTY) && StructureManager.INSTANCE.getModifiedStructures().containsKey(id.getNamespace())) {
            StructureSettings structureSettings = StructureManager.INSTANCE.getModifiedStructures().get(id.getNamespace());
            if (id.getPath().startsWith(structureSettings.getFolder())) {
                template.get().setAuthor(StructureManager.READY + "%" + id);
                VLib.INSTANCE.getLOGGER().info("prepping template with author: {}", template.get().getAuthor());
            }
        }
        cir.setReturnValue(template);
    }

    @Override
    public boolean vlib$unloadTemplate(@NotNull StructureTemplate template) {
        Optional<ResourceLocation> key = structureRepository.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent() && entry.getValue().get().equals(template))
                .map(Map.Entry::getKey)
                .findFirst();

        key.ifPresent(this::remove);
        return key.isPresent();
    }
}
