package g_mungus.vlib.data

import g_mungus.vlib.VLib.LOGGER
import g_mungus.vlib.dimension.DimensionSettingsManager
import g_mungus.vlib.structure.StructureManager
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import java.util.function.Predicate
import kotlin.collections.component1
import kotlin.collections.component2

fun onResourceReload(resourceManager: ResourceManager) {
    LOGGER.info("VLib scanning for data")

    StructureManager.resetModifiedStructures()

    val predicate = Predicate<ResourceLocation> {true}
    resourceManager.listResources("structure-settings", predicate).forEach { (resourceLocation, resource) ->

        val structures = try {
            StructureSettings.readJson(resource.open())
        } catch (e: Exception) {
            LOGGER.error("Error occurred while loading resource json: $resourceLocation", e)
            null
        }

        if (structures != null) {
            val namespace = resourceLocation.path.split("/").last().replace(".json", "")

            StructureManager.addModifiedStructures(namespace, structures)
        } else {
            LOGGER.warn("Skipping resource at $resourceLocation because it could not be parsed.")
        }
    }

    resourceManager.listResources("dimension-settings", predicate).forEach { (resourceLocation, resource) ->
        val settings = try {
            DimensionSettings.readJson(resource.open())
        } catch (e: Exception) {
            LOGGER.error("Error occurred while loading resource json: $resourceLocation", e)
            null
        }

        val location = ResourceLocation.tryParse(
            resourceLocation.path
                .drop(19)
                .replace(".json", "")
                .replace("/", ":")
        )

        if (settings != null && location != null) {

            DimensionSettingsManager.addSettings(location, settings)
        } else {
            LOGGER.warn("Skipping resource at $resourceLocation because it could not be parsed.")
        }
    }

    LOGGER.info("Finished reload. Modified structure data:\n" + StructureManager.getModifiedStructures().toString() + "\nModified dimensions:\n" + DimensionSettingsManager.getModifiedDimensions().toString())
}