package g_mungus.vlib.data

data class StructureDirectories (
    val directories: List<StructureSettings>
)

data class StructureSettings (
    val location: String,
    val placement: PlacementSettings?
)

data class PlacementSettings (
    val rename: Boolean
)
