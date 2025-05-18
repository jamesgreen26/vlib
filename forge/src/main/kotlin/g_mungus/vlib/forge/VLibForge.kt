package g_mungus.vlib.forge

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import g_mungus.vlib.VLib
import g_mungus.vlib.VLib.init
import g_mungus.vlib.VLib.initClient
import g_mungus.vlib.block.GhostPlatformBlock
import g_mungus.vlib.item.TestingStickItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(VLib.MOD_ID)
class VLibForge {
    private val itemRegister: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, VLib.MOD_ID)
    private val blockRegister: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, VLib.MOD_ID)

    private val GHOST_BLOCK: RegistryObject<GhostPlatformBlock> = blockRegister.register("ghost_block") { GhostPlatformBlock() }
    private val TESTING_STICK: RegistryObject<TestingStickItem> = itemRegister.register("testing_stick") { TestingStickItem() }

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
            VLib.TESTING_STICK = this.TESTING_STICK.get()
        }

        init()
    }

    private fun clientSetup(event: FMLClientSetupEvent?) {
        initClient()
    }
}
