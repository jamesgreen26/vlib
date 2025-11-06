package g_mungus.vlib.fabric

import g_mungus.vlib.VLib
import g_mungus.vlib.VLib.init
import g_mungus.vlib.VLib.initClient
import g_mungus.vlib.v2.internal.block.GhostPlatformBlock
import g_mungus.vlib.data.onResourceReload
import g_mungus.vlib.item.AssemblyStickItem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.valkyrienskies.mod.fabric.common.ValkyrienSkiesModFabric

object VLibFabric: ModInitializer {
    override fun onInitialize() {
        // force VS2 to load before eureka
        ValkyrienSkiesModFabric().onInitialize()

        VLib.GHOST_BLOCK = registerBlock("ghost_block", GhostPlatformBlock())
        VLib.ASSEMBLY_STICK = registerItem("assembly_stick", AssemblyStickItem())

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .register(ItemGroupEvents.ModifyEntries { entries: FabricItemGroupEntries? ->
                entries!!.accept(VLib.ASSEMBLY_STICK)
            })

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
            override fun getFabricId(): ResourceLocation? {
                return ResourceLocation(VLib.MOD_ID, "")
            }

            override fun onResourceManagerReload(resourceManager: ResourceManager) {
                onResourceReload(resourceManager)
            }
        })

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
