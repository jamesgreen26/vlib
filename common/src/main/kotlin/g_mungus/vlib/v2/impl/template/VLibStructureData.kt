package g_mungus.vlib.v2.impl.template

import kotlinx.serialization.Serializable

@Serializable
data class VLibStructureData(val isShip: Boolean = false) {
    companion object {
        val DEFAULT = VLibStructureData()
    }
}
