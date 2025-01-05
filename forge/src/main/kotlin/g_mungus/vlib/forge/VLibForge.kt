package g_mungus.vlib.forge

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import g_mungus.vlib.VLib
import g_mungus.vlib.VLib.init
import g_mungus.vlib.VLib.initClient
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(VLib.MOD_ID)
class VLibForge {
    init {
        MOD_BUS.addListener { event: FMLClientSetupEvent? ->
            clientSetup(
                event
            )
        }
        init()
    }

    private fun clientSetup(event: FMLClientSetupEvent?) {
        initClient()
    }

    companion object {
        fun getModBus(): IEventBus = MOD_BUS
    }
}
