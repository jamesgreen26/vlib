package g_mungus.vlib.data

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream

/**
 * Holds information about which structure templates should be placed in the shipyard instead of normally in the world.
 *
 * @property rename Whether the ship should be renamed to match its template name after creation. Defaults to false. Functionality not yet implemented.
 * @property folder Which folder within data/<namespace>/structures/ should have its templates placed in the shipyard. To specify all, set this value to "/".
 */
data class StructureSettings(
    val rename: Boolean?,
    val folder: String,
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