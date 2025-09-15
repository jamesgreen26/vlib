package g_mungus.vlib.dimension

import g_mungus.vlib.data.DimensionSettings
import net.minecraft.resources.ResourceLocation

object DimensionSettingsManager {

    private var modifiedDimensions = mutableMapOf<String, DimensionSettings>()

    private val DEFAULT = DimensionSettings(1.0, 1.0)

    fun addSettings(dimensionID: ResourceLocation, settings: DimensionSettings) {
        modifiedDimensions[dimensionID.toString()] = settings
    }

    fun getModifiedDimensions(): Map<String, DimensionSettings> {
        return this.modifiedDimensions.toMap()
    }

    fun getSettingsForLevel(dimensionKey: String): DimensionSettings {
        val key = dimensionKey.drop(20)
        return modifiedDimensions[key] ?: DEFAULT
    }
}