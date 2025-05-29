package g_mungus.vlib.mixin.shipPlacement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;

import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

import static net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType.JIGSAW;


@Mixin(StructureStart.class)
public class StructureStartMixin {

    @Final
    @Shadow
    private PiecesContainer pieceContainer;

    @Final
    @Shadow
    private Structure structure;

    @Inject(
            method = "placeInChunk",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPlaceInChunk(
            WorldGenLevel worldGenLevel,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource randomSource,
            BoundingBox boundingBox,
            ChunkPos chunkPos,
            CallbackInfo ci
    ) {
        List<StructurePiece> list = this.pieceContainer.pieces();

        if (
                list.stream().anyMatch(structurePiece -> structurePiece.getType() == JIGSAW)
                        && this.structure.step() == GenerationStep.Decoration.RAW_GENERATION
        ) {


            if (!list.isEmpty()) {
                BoundingBox boundingBox2 = (list.get(0)).getBoundingBox();
                BlockPos blockPos = boundingBox2.getCenter();
                BlockPos blockPos2 = new BlockPos(blockPos.getX(), boundingBox2.minY(), blockPos.getZ());

                for (StructurePiece structurePiece : list) {
                    structurePiece.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos2);
                }

                this.structure.afterPlace(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, this.pieceContainer);
            }
            ci.cancel();
        }
    }
}