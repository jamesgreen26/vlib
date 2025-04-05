package g_mungus.vlib.util

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate

interface CanRemoveTemplate {
    fun `vlib$unloadTemplate`(template: StructureTemplate): Boolean
}