package g_mungus.vlib.fabric

import g_mungus.vlib.VLib
import g_mungus.vlib.VLib.init
import g_mungus.vlib.VLib.initClient
import g_mungus.vlib.block.GhostPlatformBlock
import g_mungus.vlib.item.AssemblyStickItem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.valkyrienskies.mod.fabric.common.ValkyrienSkiesModFabric

object VLibFabric: ModInitializer {
    override fun onInitialize() {
        // force VS2 to load before eureka
        ValkyrienSkiesModFabric().onInitialize()

        VLib.GHOST_BLOCK = registerBlock("ghost_block", GhostPlatformBlock())
        VLib.ASSEMBLY_STICK = registerItem("assembly_stick", AssemblyStickItem())

        init()
    }

    @Environment(EnvType.CLIENT)
    class Client : ClientModInitializer {
        override fun onInitializeClient() {
            initClient()
        }
    }

    private fun registerBlock(name: String, block: Block): Block {
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.tryBuild(VLib.MOD_ID, name)!!, block)
    }

    private fun registerItem(name: String, item: Item): Item {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.tryBuild(VLib.MOD_ID, name)!!, item)
    }
}
