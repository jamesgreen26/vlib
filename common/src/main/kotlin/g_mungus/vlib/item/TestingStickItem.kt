package g_mungus.vlib.item

import g_mungus.vlib.api.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import org.valkyrienskies.mod.common.isBlockInShipyard

class TestingStickItem(properties: Properties) : Item(properties) {

    override fun useOn(context: UseOnContext): InteractionResult {
        if (context.level.isBlockInShipyard(context.clickedPos) && context.level is ServerLevel) {
            saveShipToTemplate("", context.level as ServerLevel, context.clickedPos)
            println("used successfully")
            return InteractionResult.SUCCESS
        }
        println("ship not found")
        return super.useOn(context)
    }

//    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
//        if (level is ServerLevel) {
//            val blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY)
//            if (level.isBlockInShipyard(blockHitResult.blockPos)) {
//                saveShipToTemplate("", level, blockHitResult.blockPos)
//                println("used successfully")
//                return InteractionResultHolder.success(player.getItemInHand(usedHand))
//            }
//        }
//
//
//        println("ship not found")
//        return super.use(level, player, usedHand)
//    }
}