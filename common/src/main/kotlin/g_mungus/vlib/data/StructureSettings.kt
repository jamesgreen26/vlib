package g_mungus.vlib.data

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream

/**
 * Holds information about which structure templates should be placed in the shipyard instead of normally in the world.
 *
 * @property folder Which folder within data/<namespace>/structures/ should have its templates placed in the shipyard. To specify all, set this value to "/".
 * @property rename Whether the ship should be renamed to match its template name after creation. Defaults to false.
 * @property static Whether the ship should be left static after placement. Defaults to false.
 */
data class StructureSettings(
    val folder: String,
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