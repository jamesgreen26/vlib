package g_mungus.vlib.item

import g_mungus.vlib.VLib.LOGGER
import g_mungus.vlib.api.VLibGameUtils
import g_mungus.vlib.util.findConnectedBlocks
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

class TestingStickItem(properties: Properties) : Item(properties) {

    override fun useOn(context: UseOnContext): InteractionResult {

        if (context.level is ServerLevel) {
            VLibGameUtils.assembleByConnectivity(context.level as ServerLevel, context.clickedPos)
        }

        context.player?.swing(context.hand)
        return super.useOn(context)
    }

}