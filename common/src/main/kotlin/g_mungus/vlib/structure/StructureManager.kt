package g_mungus.vlib.structure

import g_mungus.vlib.VLib.LOGGER
import g_mungus.vlib.data.StructureSettings

object StructureManager {
    @Volatile
    private var modifiedStructures = mapOf<String, StructureSettings>()

    fun addModifiedStructures(modifiedStructures: Map<String, StructureSettings>) {
        this.modifiedStructures = this.modifiedStructures.toMutableMap() + modifiedStructures
    }

    fun resetModifiedStructures() {
        modifiedStructures = mapOf()
    }

    fun getModifiedStructures(): Map<String, StructureSettings> {
        return this.modifiedStructures.toMap()
    }
}