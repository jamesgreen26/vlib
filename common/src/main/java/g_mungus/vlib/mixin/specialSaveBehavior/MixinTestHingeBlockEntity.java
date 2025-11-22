package g_mungus.vlib.mixin.specialSaveBehavior;

import g_mungus.vlib.VLib;
import g_mungus.vlib.v2.api.VLibAPI;
import g_mungus.vlib.v2.api.experimental.HasSpecialSaveBehavior;
import g_mungus.vlib.v2.api.extension.NBTExtKt;
import g_mungus.vlib.v2.api.extension.ServerLevelExtKt;
import g_mungus.vlib.v2.api.extension.ShipExtKt;
import kotlin.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.block.TestHingeBlock;
import org.valkyrienskies.mod.common.blockentity.TestHingeBlockEntity;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.Objects;

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
    public void executeWhenSavingShip() {
        BlockPos hingePos = getOtherHingePos();
        if (getLevel() instanceof ServerLevel serverLevel && hingePos != null) {
            ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, getOtherHingePos());
            if (ship != null) {
                ResourceLocation location = new ResourceLocation(VLib.MOD_ID, Objects.requireNonNull(ship.getSlug()));
                BlockPos corner = ShipExtKt.getTemplateCorner(ship);

                VLibAPI.saveShipToTemplate(ship, location, serverLevel);
                VLibAPI.discardShip(ship, serverLevel);

                vlib$constraintTemplate = location;
                vlib$templateOffset = hingePos.subtract(corner);
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

                if (ship != null) {
                    ServerLevelExtKt.scheduleCallback(serverLevel, 3, () -> {
                        structureTemplate.placeInWorld(serverLevel, structurePos, structurePos, new StructurePlaceSettings(), serverLevel.random, 0);
                        Vector3dc positionInWorld = ship.getTransform().getPositionInWorld();
                        Vector3dc targetPos = getTargetPos(serverLevel);

                        Vector3dc cogInWorld = ship.getShipToWorld().transformPosition(ship.getInertiaData().getCenterOfMassInShip(), new Vector3d());


                        ShipExtKt.teleport(ship, serverLevel, targetPos.add(cogInWorld, new Vector3d()).add(0.5, 0.5, 0.5).sub(positionInWorld));
                        return Unit.INSTANCE;
                    });



                } else { VLib.INSTANCE.getLOGGER().error("SHIP IS NULL");}
            });
        }
    }

    @Unique
    Vector3dc getTargetPos(ServerLevel serverLevel) {
        ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, getBlockPos());
        BlockPos blockPos = getBlockPos().offset(getBlockState().getValue(DirectionalBlock.FACING).getNormal());
        Vector3d pos = VectorConversionsMCKt.toJOML(blockPos.getCenter());
        return ship != null ? ship.getShipToWorld().transformPosition(pos, new Vector3d()) : pos;
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
