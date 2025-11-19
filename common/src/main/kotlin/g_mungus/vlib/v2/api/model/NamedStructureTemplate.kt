package g_mungus.vlib.v2.api.model

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate

data class NamedStructureTemplate (val template: StructureTemplate, val resourceLocation: ResourceLocation)