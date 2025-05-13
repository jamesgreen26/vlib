package g_mungus.vlib.mixin.shipAssembly;

import com.google.common.collect.Lists;
import g_mungus.vlib.VLib;
import g_mungus.vlib.util.CanFillByConnectivity;
import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


import static g_mungus.vlib.util.ConnectivityKt.findConnectedBlocks;

@Mixin(value = StructureTemplate.class)
public abstract class StructureTemplateMixin implements CanFillByConnectivity {

    @Shadow
    private Vec3i size;

    @Final
    @Shadow
    private List<StructureTemplate.Palette> palettes;

    @Final
    @Shadow
    private List<StructureTemplate.StructureEntityInfo> entityInfoList;

    @Shadow
    private static List<StructureTemplate.StructureBlockInfo> buildInfoList(List<StructureTemplate.StructureBlockInfo> basicBlocks, List<StructureTemplate.StructureBlockInfo> blocksWithEntities, List<StructureTemplate.StructureBlockInfo> specialBlocks) {
        return null;
    }

    @Shadow
    private static void addToLists(StructureTemplate.StructureBlockInfo blockInfo, List<StructureTemplate.StructureBlockInfo> basicBlocks, List<StructureTemplate.StructureBlockInfo> blocksWithEntities, List<StructureTemplate.StructureBlockInfo> specialBlocks) {}

    @Unique
    private static Constructor<?> vlib$paletteConstructor = null;

    @Unique
    private static StructureTemplate.Palette vlib$newPalette(List<StructureTemplate.StructureBlockInfo> blockInfoList) {
        try {
            if (vlib$paletteConstructor == null) {
                vlib$paletteConstructor = StructureTemplate.Palette.class.getConstructors()[0];
            }
            return (StructureTemplate.Palette) vlib$paletteConstructor.newInstance(blockInfoList);
        } catch (Exception e) {
            VLib.INSTANCE.getLOGGER().error("Uh oh", e);
            return null;
        }
    }

    @Override
    @Unique
    public CompletionStage<Pair<Set<BlockPos>, BlockPos>> vlib$fillByConnectivity(Level level, BlockPos pos) {
        CompletableFuture<Pair<Set<BlockPos>, BlockPos>> output = new CompletableFuture<>();
        findConnectedBlocks(level, pos).whenComplete((res, throwable) -> {
            if (throwable != null) {
                output.completeExceptionally(throwable);
            } else {
                BlockPos minCorner = VectorConversionsMCKt.toBlockPos(res.getMiddle());
                BlockPos maxCorner = VectorConversionsMCKt.toBlockPos(res.getRight());

                List<StructureTemplate.StructureBlockInfo> basicBlocks = Lists.newArrayList();
                List<StructureTemplate.StructureBlockInfo> blocksWithEntities = Lists.newArrayList();
                List<StructureTemplate.StructureBlockInfo> specialBlocks = Lists.newArrayList();

                this.size = new Vec3i(
                        maxCorner.getX() - minCorner.getX() + 1,
                        maxCorner.getY() - minCorner.getY() + 1,
                        maxCorner.getZ() - minCorner.getZ() + 1
                );

                for (BlockPos currentWorldPos : res.getLeft()) {
                    BlockPos relativePos = currentWorldPos.subtract(minCorner);
                    BlockState blockState = level.getBlockState(currentWorldPos);

                    BlockEntity blockEntity = level.getBlockEntity(currentWorldPos);
                    StructureTemplate.StructureBlockInfo blockInfo;

                    if (blockEntity != null) {
                        blockInfo = new StructureTemplate.StructureBlockInfo(relativePos, blockState, blockEntity.saveWithId());
                    } else {
                        blockInfo = new StructureTemplate.StructureBlockInfo(relativePos, blockState, null);
                    }

                    addToLists(blockInfo, basicBlocks, blocksWithEntities, specialBlocks);
                }
                List<StructureTemplate.StructureBlockInfo> finalBlockList = buildInfoList(basicBlocks, blocksWithEntities, specialBlocks);
                this.entityInfoList.clear();
                this.palettes.clear();
                this.palettes.add(vlib$newPalette(finalBlockList));
                output.complete(new Pair<>(res.getLeft(),minCorner));
            }
        });
        return output;
    }
}


