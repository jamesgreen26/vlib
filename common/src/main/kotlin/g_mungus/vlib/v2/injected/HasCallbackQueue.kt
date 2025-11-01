package g_mungus.vlib.v2.injected

interface HasCallbackQueue {
    fun `vlib$enqueue`(ticks: Int, callback: () -> Unit)
}