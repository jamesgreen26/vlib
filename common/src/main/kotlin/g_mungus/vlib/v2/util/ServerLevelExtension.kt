package g_mungus.vlib.v2.util

import g_mungus.vlib.v2.injected.HasCallbackQueue
import net.minecraft.server.level.ServerLevel

fun ServerLevel.scheduleCallback(ticks: Int, callback: () -> Unit) {
    (this as HasCallbackQueue).`vlib$enqueue`(ticks, callback)
}