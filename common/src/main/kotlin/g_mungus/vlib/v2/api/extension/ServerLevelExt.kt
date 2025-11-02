package g_mungus.vlib.v2.api.extension

import g_mungus.vlib.v2.util.injected.HasCallbackQueue
import net.minecraft.server.level.ServerLevel

fun ServerLevel.scheduleCallback(ticks: Int, callback: () -> Unit) {
    (this as HasCallbackQueue).`vlib$enqueue`(ticks, callback)
}