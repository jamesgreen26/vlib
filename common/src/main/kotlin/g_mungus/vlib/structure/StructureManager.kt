package g_mungus.vlib.structure

import g_mungus.vlib.data.StructureSettings

object StructureManager {
    @Volatile
    private var modifiedStructures = mapOf<String, StructureSettings>()

    fun addModifiedStructures(namespace: String, structureSettings: StructureSettings) {
        val modifiedStructures = this.modifiedStructures.toMutableMap()
        modifiedStructures[namespace] = structureSettings
        this.modifiedStructures = modifiedStructures
    }

    fun resetModifiedStructures() {
        modifiedStructures = mapOf()
    }

    fun getModifiedStructures(): Map<String, StructureSettings> {
        return this.modifiedStructures.toMap()
    }
}