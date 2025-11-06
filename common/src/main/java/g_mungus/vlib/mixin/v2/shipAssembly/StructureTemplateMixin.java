package g_mungus.vlib.mixin.v2.shipAssembly;

import com.google.common.collect.Lists;
import g_mungus.vlib.v2.internal.assembly.BoundedVoxelSet;
import g_mungus.vlib.v2.internal.injected.CanFillFromVoxelSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import java.util.List;


@Mixin(value = StructureTemplate.class)
public abstract class StructureTemplateMixin implements CanFillFromVoxelSet {

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

    @Override
    @Unique
    public void vlib$fillFromVoxelSet(@NotNull Level level, @NotNull BoundedVoxelSet voxels) {
        BlockPos minCorner = VectorConversionsMCKt.toBlockPos(voxels.getMin());
        BlockPos maxCorner = VectorConversionsMCKt.toBlockPos(voxels.getMax());

        List<StructureTemplate.StructureBlockInfo> basicBlocks = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> blocksWithEntities = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> specialBlocks = Lists.newArrayList();

        this.size = new Vec3i(
                maxCorner.getX() - minCorner.getX() + 1,
                maxCorner.getY() - minCorner.getY() + 1,
                maxCorner.getZ() - minCorner.getZ() + 1
        );

        for (BlockPos currentWorldPos : voxels.getVoxels()) {
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
        this.palettes.add(PaletteInvoker.invokeInit(finalBlockList));
    }
}


