package g_mungus.vlib.data

data class StructureSettings (
    val placement: PlacementSettings?
) {
    data class PlacementSettings (
        val rename: Boolean
    )
}


