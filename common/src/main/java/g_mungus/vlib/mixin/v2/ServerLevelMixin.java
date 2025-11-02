package g_mungus.vlib.mixin.v2;

import g_mungus.vlib.v2.util.injected.HasCallbackQueue;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements HasCallbackQueue {

    @Unique
    private ConcurrentLinkedDeque<Pair<Long, Function0<Unit>>> vlib$callbacks = null;

    @Unique
    private long vlib$elapsedTicks;

    @Inject(method = "tick", at = @At("HEAD"))
    private void vlib$tick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (vlib$callbacks == null) {
            vlib$callbacks = new ConcurrentLinkedDeque<>();
            vlib$elapsedTicks = 0L;
        }

        vlib$callbacks.removeIf((it) -> {
            boolean shouldRemove = it.getFirst() <= vlib$elapsedTicks;
            if (shouldRemove) {
                it.getSecond().invoke();
            }
            return shouldRemove;
        });
        vlib$elapsedTicks++;
    }

    @Override
    public void vlib$enqueue(int ticks, @NotNull Function0<Unit> callback) {
        if (vlib$callbacks == null) {
            vlib$callbacks = new ConcurrentLinkedDeque<>();
            vlib$elapsedTicks = 0L;
        }
        vlib$callbacks.add(new Pair<>(vlib$elapsedTicks + ticks, callback));
    }
}

