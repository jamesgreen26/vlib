package g_mungus.vlib.data

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import g_mungus.vlib.VLib.LOGGER
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import com.fasterxml.jackson.module.kotlin.readValue
import g_mungus.vlib.structure.StructureManager

import java.io.InputStream
import java.util.function.Predicate


object SynchronousResourceReloadListener: SimpleSynchronousResourceReloadListener {
    override fun getFabricId(): ResourceLocation {
        return ResourceLocation("vlib", "auto-assemble")
    }

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        LOGGER.info("VLib scanning for data")
        val predicate = Predicate<ResourceLocation> {resourceLocation -> resourceLocation.path.equals("structure-settings/structures.json")}
        resourceManager.listResources("structure-settings", predicate).forEach { (resourceLocation, resource) ->

            val structures = try {
                readJsonWithJackson(resource.open())
            } catch (e: Exception) {
                LOGGER.error("Error occurred while loading resource json: $resourceLocation", e)
                null
            }

            if (structures != null) {
                StructureManager.setStructuresToAssemble(structures)
            } else {
                LOGGER.warn("Skipping resource at $resourceLocation because it could not be parsed.")
            }
        }

        LOGGER.info("Finished reload. Modified structure data:\n" + StructureManager.getStructuresToAssemble().toString())
    }

    private fun readJsonWithJackson(inputStream: InputStream): StructureDirectories? {
        val objectMapper = jacksonObjectMapper()
        return inputStream.use {
            try {
                objectMapper.readValue(it)
            } catch (e: MismatchedInputException) {
                null
            }
        }
    }
}