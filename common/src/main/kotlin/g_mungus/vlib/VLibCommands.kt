package g_mungus.vlib

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import g_mungus.vlib.util.calculateMasses
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object VLibCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("vlib")
                .then(
                    Commands.literal("generateMasses")
                        .requires { source -> source.hasPermission(2) }
                        .executes { context -> calculateMassesCommand(context) }
                )
        )
    }

    private fun calculateMassesCommand(context: CommandContext<CommandSourceStack>): Int {
        val path = calculateMasses(context.source.level)
        val formattedPath = path.split("/").takeLast(2).joinToString(separator = "/") { a -> a }
        context.source.sendSuccess({ Component.literal("Mass calculation complete!\nRelaunch the game for mass changes to take effect.") }, true)
        return 1
    }
}
