package g_mungus.vlib

import g_mungus.vlib.data.SynchronousResourceReloadListener
import g_mungus.vlib.item.TestingStickItem
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.mixin.registry.sync.RegistriesAccessor
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object VLib {
    const val MOD_ID = "vlib"
    val LOGGER: Logger = LoggerFactory.getLogger("VLib")
    const val MAX_RECURSION = Int.MAX_VALUE / 10


    @JvmStatic
    fun init(isFabric: Boolean) {
        LOGGER.info("VLib init")

        // Register data loader
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(SynchronousResourceReloadListener)


        if(isFabric) {
            registerItem("testing_stick", TestingStickItem(Item.Properties().rarity(Rarity.EPIC)))
        }
    }

    @JvmStatic
    fun initClient() {
    }


    private fun registerItem(name: String, item: Item) {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation(MOD_ID, name), item
        )
    }
}