package g_mungus.vlib.v2.internal.template

import g_mungus.vlib.VLib
import g_mungus.vlib.mixin.v2.templatePlacement.StructureTemplateAccessor
import g_mungus.vlib.v2.internal.serialization.toJson
import g_mungus.vlib.v2.internal.serialization.toTag
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.phys.Vec3

fun StructureTemplate.saveAdditional(data: VLibStructureData) {
    (this as StructureTemplateAccessor).entityInfoList.apply {
        removeIf { it.nbt.contains(VLib.MOD_ID) }
        add(StructureTemplate.StructureEntityInfo(
            Vec3(0.0, 0.0, 0.0),
            BlockPos(0,0,0),
            CompoundTag().apply {put(VLib.MOD_ID, Json.encodeToJsonElement(data).toTag())}
        ))
    }
}

fun StructureTemplate.readAdditional(): VLibStructureData {
    (this as StructureTemplateAccessor).entityInfoList.forEach { entityInfo ->
        if (entityInfo.nbt.contains(VLib.MOD_ID)) {
            return Json.decodeFromJsonElement(entityInfo.nbt.getCompound(VLib.MOD_ID).toJson())
        }
    }
    return VLibStructureData.DEFAULT
}