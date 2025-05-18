package g_mungus.vlib

import g_mungus.vlib.data.SynchronousResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object VLib {
    const val MOD_ID = "vlib"
    val LOGGER: Logger = LoggerFactory.getLogger("VLib")
    const val MAX_RECURSION = Short.MAX_VALUE * 4

    lateinit var GHOST_BLOCK: Block
    lateinit var ASSEMBLY_STICK: Item

    @JvmStatic
    fun init() {
        LOGGER.info("VLib init")

        // Register data loader
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(SynchronousResourceReloadListener)
    }

    @JvmStatic
    fun initClient() {
    }
}