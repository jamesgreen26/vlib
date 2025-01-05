package g_mungus.vlib.structure

import g_mungus.vlib.data.StructureDirectories
import g_mungus.vlib.data.StructureSettings

object StructureManager {
    private var modifiedStructures = StructureDirectories(directories = listOf<StructureSettings>())

    fun setStructuresToAssemble (modifiedStructures: StructureDirectories) {
        this.modifiedStructures = modifiedStructures
    }

    fun getStructuresToAssemble(): StructureDirectories {
        return this.modifiedStructures
    }
}