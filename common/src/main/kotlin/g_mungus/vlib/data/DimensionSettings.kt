package g_mungus.vlib.data

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream


data class DimensionSettings(val shipScale: Double, val gravity: Double) {

    companion object {
        private val objectMapper = jacksonObjectMapper()

        fun readJson(inputStream: InputStream): DimensionSettings? {
            return inputStream.use {
                try {
                    objectMapper.readValue(it, DimensionSettings::class.java)
                } catch (e: MismatchedInputException) {
                    null
                }
            }
        }
    }
}