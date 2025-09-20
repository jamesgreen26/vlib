package g_mungus.vlib

import g_mungus.vlib.dimension.GravityAttachment
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.valkyrienskies.core.impl.hooks.VSEvents
import org.valkyrienskies.mod.common.ValkyrienSkiesMod


object VLib {
    const val MOD_ID = "vlib"
    val LOGGER: Logger = LoggerFactory.getLogger("VLib")
    const val MAX_RECURSION = Short.MAX_VALUE * 4

    lateinit var GHOST_BLOCK: Block
    lateinit var ASSEMBLY_STICK: Item

    @JvmStatic
    fun init() {
        LOGGER.info("VLib init")

        VSEvents.shipLoadEvent.on { event ->
            GravityAttachment.getOrCreate(ship = event.ship)
        }
    }

    @JvmStatic
    fun initClient() { }

    @JvmStatic
    val isDuringWorldGen get() = Thread.currentThread() != ValkyrienSkiesMod.currentServer?.runningThread
}