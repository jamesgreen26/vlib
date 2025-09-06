package g_mungus.vlib.data


import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream

/**
 * Custom deserializer to handle folder/folders as either String or List<String>
 */
class FolderListDeserializer : JsonDeserializer<List<String>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<String> {
        val node: JsonNode = p.codec.readTree(p)
        return when {
            node.isTextual -> listOf(node.asText())
            node.isArray -> node.map { it.asText() }
            else -> listOf() // default value
        }
    }
}

/**
 * Holds information about which structure templates should be placed in the shipyard instead of normally in the world.
 *
 * @property folders Which folders within data/<namespace>/structures/ should have their templates placed in the shipyard. To specify all, set this value to "/".
 * @property rename Whether the ship should be renamed to match its template name after creation. Defaults to false.
 * @property static Whether the ship should be left static after placement. Defaults to false.
 */
data class StructureSettings(
    @JsonAlias("folder") // This allows both "folder" and "folders" keys in JSON
    @JsonDeserialize(using = FolderListDeserializer::class)
    val folders: List<String>,
    val rename: Boolean?,
    val static: Boolean?,
) {
    companion object {
        fun readJson(inputStream: InputStream): StructureSettings? {
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
}