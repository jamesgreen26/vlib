package g_mungus.vlib.mixin.shipPlacement;

import g_mungus.vlib.structure.StructureManager;
import kotlin.Triple;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    protected void tickMixin(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (!StructureManager.INSTANCE.getAssemblyQueue().isEmpty()) {
            Triple<StructureTemplate, ServerLevel, BlockPos> structureData = StructureManager.INSTANCE.getAssemblyQueue().poll();
            assert structureData != null;
            StructureManager.INSTANCE.createShipFromTemplate(structureData.getFirst(), structureData.getSecond(), structureData.getThird());
        }
    }
}
