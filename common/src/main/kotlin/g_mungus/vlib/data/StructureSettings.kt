package g_mungus.vlib.data


/**
 * Represents the custom settings for structures in the given namespace.
 *
 * @property placement Holds information about which structure templates should be placed in the shipyard instead of normally in the world.
 */
data class StructureSettings (
    @get:JvmName("getPlacementSettings") val placement: PlacementSettings?
) {
    /**
     * Holds information about which structure templates should be placed in the shipyard instead of normally in the world.
     *
     * @property rename Whether the ship should be renamed to match its template name after creation. Defaults to false.
     * @property folder Which folder within data/<namespace>/structures/ should have its templates placed in the shipyard. To specify all, set this value to "/".
     */
    data class PlacementSettings (
        @get:JvmName("shouldRename") val rename: Boolean?,
        @get:JvmName("getAutoAssemblyFolder") val folder: String,
    )
}


