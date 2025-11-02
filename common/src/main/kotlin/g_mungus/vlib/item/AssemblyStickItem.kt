package g_mungus.vlib.item

import g_mungus.vlib.v2.api.VLibAPI
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.context.UseOnContext

class AssemblyStickItem : Item(Properties().rarity(Rarity.EPIC)) {

    override fun useOn(context: UseOnContext): InteractionResult {

        if (context.level is ServerLevel) {
            VLibAPI.assembleByConnectivity(context.level as ServerLevel, context.clickedPos)
        }

        context.player?.swing(context.hand)
        return super.useOn(context)
    }

}