package g_mungus.vlib.mixin.specialSaveBehavior;

import g_mungus.vlib.VLib;
import g_mungus.vlib.api.HasSpecialSaveBehavior;
import g_mungus.vlib.api.VLibGameUtils;
import g_mungus.vlib.v2.util.NBTExtKt;
import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.block.TestHingeBlock;
import org.valkyrienskies.mod.common.blockentity.TestHingeBlockEntity;

@Mixin(value = TestHingeBlockEntity.class, remap = false)
public abstract class MixinTestHingeBlockEntity extends BlockEntity implements HasSpecialSaveBehavior {

    @Shadow
    public BlockPos getOtherHingePos() {return null;}

    public MixinTestHingeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Unique
    private ResourceLocation vlib$constraintTemplate = null;

    @Unique
    private BlockPos vlib$templateOffset = null;

    @Unique
    private final String vlib$templateKey = "vlib$templateLocation", vlib$offsetKey = "vlib$structure_offset";

    @Override
    public void executeWhenSavingShip(long parentShipId) {
        BlockPos hingePos = getOtherHingePos();
        if (getLevel() instanceof ServerLevel serverLevel && hingePos != null) {
            Pair<ResourceLocation, BlockPos> res = VLibGameUtils.INSTANCE.saveShipToTemplate2(VLib.MOD_ID, serverLevel, hingePos, false, true);
            if (res != null) {
                vlib$constraintTemplate = res.getFirst();
                vlib$templateOffset = hingePos.subtract(res.getSecond());
            }
        }
    }

    @Override
    public void executeAfterLoadingShip() {
        if (getLevel() instanceof ServerLevel serverLevel && vlib$constraintTemplate != null) {
            ServerPlayer player = serverLevel.getPlayers(it -> true).get(0);

            TestHingeBlock block = (TestHingeBlock) getBlockState().getBlock();
            block.use(getBlockState(), serverLevel, getBlockPos(), player, InteractionHand.MAIN_HAND, BlockHitResult.miss(player.position(), player.getDirection(), player.blockPosition()));

            serverLevel.getStructureManager().get(vlib$constraintTemplate).ifPresent(structureTemplate -> {
                BlockPos hingePos = getOtherHingePos();
                BlockPos structurePos = hingePos.subtract(vlib$templateOffset);
                ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, hingePos);

                if (ship != null) { ship.setStatic(true); } else { VLib.INSTANCE.getLOGGER().error("SHIP IS NULL");}
                structureTemplate.placeInWorld(serverLevel, structurePos, structurePos, new StructurePlaceSettings(), serverLevel.random, 0);

            });
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (vlib$constraintTemplate != null) {
            tag.putString(vlib$templateKey, vlib$constraintTemplate.toString());
            NBTExtKt.putVec3i(tag, vlib$offsetKey, vlib$templateOffset);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        Tag template = tag.get(vlib$templateKey);
        if (template != null) {
            vlib$constraintTemplate = new ResourceLocation(template.getAsString());

            Vec3i vec = NBTExtKt.getVec3i(tag, vlib$offsetKey);
            vlib$templateOffset = vec != null ? new BlockPos(vec) : null;
        }
    }
}
