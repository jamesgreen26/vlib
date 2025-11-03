package g_mungus.vlib.mixin.v2.templatePlacement;

import g_mungus.vlib.v2.impl.template.StructureTemplateExtKt;
import g_mungus.vlib.v2.util.injected.HasCallbackQueue;
import kotlin.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Unique
    private static final ConcurrentHashMap<BlockPos, Boolean> vlib$blacklist = new ConcurrentHashMap<>();

    @Inject(method = "placeInWorld", at = @At("HEAD"), cancellable = true)
    void redirectToShipPlacement(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int i, CallbackInfoReturnable<Boolean> cir) {
        StructureTemplate template = (StructureTemplate) (Object) this;

        if (StructureTemplateExtKt.readAdditional(template).isShip() && serverLevelAccessor instanceof WorldGenLevel) {
            ServerLevel serverLevel = serverLevelAccessor.getLevel();

            if (!VSGameUtilsKt.isBlockInShipyard(serverLevel, blockPos)) {

                if (serverLevelAccessor instanceof ServerLevel) {
                    g_mungus.vlib.v2.api.extension.StructureTemplateExtKt.placeAsShip(template, serverLevel, blockPos, false);
                    cir.setReturnValue(true);
                } else if (vlib$blacklist.putIfAbsent(blockPos, Boolean.TRUE) == null) {
                    // world gen is messy, only allow one structure per block pos

                    // create ship on the main server thread to avoid issues with world gen
                    ((HasCallbackQueue) serverLevel).vlib$enqueue(0, () -> {
                        g_mungus.vlib.v2.api.extension.StructureTemplateExtKt.placeAsShip(template, serverLevel, blockPos, false);
                        return Unit.INSTANCE;
                    });
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
