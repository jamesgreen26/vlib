package g_mungus.vlib.mixin.shipPlacement;

import g_mungus.vlib.structure.StructureManager;
import g_mungus.vlib.structure.TemplateAssemblyData;
import net.minecraft.server.level.ServerLevel;
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
            TemplateAssemblyData data = StructureManager.INSTANCE.getAssemblyQueue().poll();
            assert data != null;
            StructureManager.INSTANCE.createShipFromTemplate(data);
        }
    }
}
