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
        return ResourceLocation("vlib", "")
    }

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        LOGGER.info("VLib scanning for data")

        StructureManager.resetModifiedStructures()

        val predicate = Predicate<ResourceLocation> {true}
        resourceManager.listResources("structure-settings", predicate).forEach { (resourceLocation, resource) ->

            val structures = try {
                readJsonWithJackson(resource.open())
            } catch (e: Exception) {
                LOGGER.error("Error occurred while loading resource json: $resourceLocation", e)
                null
            }

            if (structures != null) {
                StructureManager.addModifiedStructures(resourceLocation.namespace, structures)
            } else {
                LOGGER.warn("Skipping resource at $resourceLocation because it could not be parsed.")
            }
        }

        LOGGER.info("Finished reload. Modified structure data:\n" + StructureManager.getModifiedStructures().toString())
    }

    private fun readJsonWithJackson(inputStream: InputStream): StructureSettings? {
        val objectMapper = jacksonObjectMapper()
        return inputStream.use {
            try {
                objectMapper.readValue(it, StructureSettings::class.java)
            } catch (e: MismatchedInputException) {
                null
            }
        }
    }
}