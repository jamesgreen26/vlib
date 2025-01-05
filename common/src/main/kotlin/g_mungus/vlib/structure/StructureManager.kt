package g_mungus.vlib.structure

import g_mungus.vlib.data.StructureDirectories
import g_mungus.vlib.data.StructureSettings

object StructureManager {
    private var modifiedStructures = StructureDirectories(directories = listOf<StructureSettings>())

    fun addModifiedStructures (modifiedStructures: StructureDirectories) {
        TODO()
    }

    fun resetModifiedStructures () {
        modifiedStructures = StructureDirectories(directories = listOf<StructureSettings>())
    }

    fun getModifiedStructures(): StructureDirectories {
        return this.modifiedStructures
    }
}