package g_mungus.vlib.mixin.shipPlacement;

import g_mungus.vlib.VLib;
import g_mungus.vlib.structure.StructureManager;
import g_mungus.vlib.structure.TemplateAssemblyData;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {


    @Inject(method = "tickServer", at = @At("HEAD"))
    protected void tickMixin(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (!StructureManager.INSTANCE.getAssemblyQueue().isEmpty()) {
            TemplateAssemblyData data = StructureManager.INSTANCE.getAssemblyQueue().poll();
            assert data != null;
            StructureManager.INSTANCE.createShipFromTemplate(data);
        }

        long now = new Date().getTime();
        StructureManager.INSTANCE.getBlacklist().forEach((blockPos, timestamp) -> {
            if (now > timestamp + 300_000) {
                StructureManager.INSTANCE.getBlacklist().remove(blockPos);
                VLib.INSTANCE.getLOGGER().info("{} is now free", blockPos.toShortString());
            }
        });
    }
}
