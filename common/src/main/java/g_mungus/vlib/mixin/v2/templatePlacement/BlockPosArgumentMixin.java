package g_mungus.vlib.mixin.v2.templatePlacement;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import g_mungus.vlib.v2.internal.template.StructureTemplateExtKt;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.ERROR_NOT_LOADED;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.getBlockPos;

@Mixin(BlockPosArgument.class)
public class BlockPosArgumentMixin {
    @Inject(
            method = "getLoadedBlockPos(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/level/ServerLevel;Ljava/lang/String;)Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onGetLoadedBlockPos(
            CommandContext<CommandSourceStack> commandContext, ServerLevel serverLevel, String string, CallbackInfoReturnable<BlockPos> cir
    ) throws CommandSyntaxException {

        AtomicBoolean shouldBypassHeightCheck = new AtomicBoolean(false);

        try {
            ResourceLocation id = ResourceLocationArgument.getId(commandContext, "template");

            serverLevel.getStructureManager().get(id).ifPresent((template) -> {
                shouldBypassHeightCheck.set(StructureTemplateExtKt.readAdditional(template).isShip());
            });

        } catch (Exception e) {
            // command does not contain a template id, so we can ignore it
        }

        if (shouldBypassHeightCheck.get()) {
            BlockPos blockPos = getBlockPos(commandContext, string);
            if (!serverLevel.hasChunkAt(blockPos)) {
                throw ERROR_NOT_LOADED.create();
            } else {
                cir.setReturnValue(blockPos);
            }
        }
    }
}
