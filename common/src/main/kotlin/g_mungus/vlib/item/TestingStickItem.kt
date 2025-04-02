package g_mungus.vlib.item

import g_mungus.vlib.api.*
import g_mungus.vlib.api.VLibGameUtils.saveShipToTemplate
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import org.valkyrienskies.mod.common.isBlockInShipyard

class TestingStickItem(properties: Properties) : Item(properties) {

    override fun useOn(context: UseOnContext): InteractionResult {
        if (context.level.isBlockInShipyard(context.clickedPos) && context.level is ServerLevel) {
            saveShipToTemplate("vlib:ships/", context.level as ServerLevel, context.clickedPos, withEntities = false, deleteAfter = true)
            println("used successfully")
            return InteractionResult.SUCCESS
        }
        if (context.level is ServerLevel) println("ship not found")
        return super.useOn(context)
    }

}