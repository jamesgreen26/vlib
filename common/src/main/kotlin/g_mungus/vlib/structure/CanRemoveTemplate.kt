package g_mungus.vlib.structure

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate

interface CanRemoveTemplate {
    fun `vlib$unloadTemplate`(template: StructureTemplate): Boolean
}