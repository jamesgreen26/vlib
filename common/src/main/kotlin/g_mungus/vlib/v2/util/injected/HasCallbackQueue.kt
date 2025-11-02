package g_mungus.vlib.v2.util.injected

interface HasCallbackQueue {
    fun `vlib$enqueue`(ticks: Int, callback: () -> Unit)
}