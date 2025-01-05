package g_mungus.vlib.fabric

import g_mungus.vlib.VLib.init
import g_mungus.vlib.VLib.initClient
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import org.valkyrienskies.mod.fabric.common.ValkyrienSkiesModFabric

object VLibFabric: ModInitializer {
    override fun onInitialize() {
        // force VS2 to load before eureka
        ValkyrienSkiesModFabric().onInitialize()

        init()
    }

    @Environment(EnvType.CLIENT)
    class Client : ClientModInitializer {
        override fun onInitializeClient() {
            initClient()
        }
    }
}
