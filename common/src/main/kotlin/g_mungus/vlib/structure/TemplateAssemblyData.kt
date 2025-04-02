package g_mungus.vlib.structure

import g_mungus.vlib.VLib
import g_mungus.vlib.data.StructureSettings
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship

class TemplateAssemblyData (
    val template: StructureTemplate,
    val id: ResourceLocation,
    val level: ServerLevel,
    val pos: BlockPos,
    val structureSettings: StructureSettings
) {
    fun callback(ship: Ship) {
        if (structureSettings.rename == true) {
            val name = getName(id.path)
            if (name != null) {
                (ship as ServerShip).slug = name
            } else {
                VLib.LOGGER.warn("Failed to rename ship")
            }
        }
        if (structureSettings.static != true) {
            (ship as ServerShip).isStatic = false
        }
    }


    private fun getName(location: String): String? {
        val i = location.lastIndexOf('/')
        if (i >= 0) {
            return location.substring(i + 1)
        }
        return null
    }
}

