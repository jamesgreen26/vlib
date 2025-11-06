package g_mungus.vlib.forge

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.event.AddReloadListenerEvent
import g_mungus.vlib.VLib
import g_mungus.vlib.VLib.init
import g_mungus.vlib.VLib.initClient
import g_mungus.vlib.v2.internal.block.GhostPlatformBlock
import g_mungus.vlib.data.onResourceReload
import g_mungus.vlib.item.AssemblyStickItem
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(VLib.MOD_ID)
class VLibForge {
    private val itemRegister: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, VLib.MOD_ID)
    private val blockRegister: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, VLib.MOD_ID)

    private val GHOST_BLOCK: RegistryObject<GhostPlatformBlock> = blockRegister.register("ghost_block") { GhostPlatformBlock() }
    private val ASSEMBLY_STICK: RegistryObject<AssemblyStickItem> = itemRegister.register("assembly_stick") { AssemblyStickItem() }

    init {
        itemRegister.register(MOD_BUS)
        blockRegister.register(MOD_BUS)

        MOD_BUS.addListener { event: FMLClientSetupEvent? ->
            clientSetup(
                event
            )
        }

        MOD_BUS.addListener { event: FMLCommonSetupEvent ->
            VLib.GHOST_BLOCK = this.GHOST_BLOCK.get()
            VLib.ASSEMBLY_STICK = this.ASSEMBLY_STICK.get()
        }

        MOD_BUS.addListener { event: BuildCreativeModeTabContentsEvent ->
            if (event.tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(VLib.ASSEMBLY_STICK)
            }
        }

        FORGE_BUS.addListener { event: AddReloadListenerEvent ->
            event.addListener(object : ResourceManagerReloadListener {
                override fun onResourceManagerReload(resourceManager: ResourceManager) {
                    onResourceReload(resourceManager)
                }
            })
        }

        init()
    }

    private fun clientSetup(event: FMLClientSetupEvent?) {
        initClient()
    }
}
