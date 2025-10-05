package g_mungus.vlib.mixin.playerRotation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.mod.client.IVSCamera;
import org.valkyrienskies.mod.common.VSClientGameUtils;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Math;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    @Final
    Minecraft minecraft;
    // region Mount the camera to the ship
    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    protected abstract double getFov(Camera camera, float f, boolean bl);

    @Shadow
    public abstract Matrix4f getProjectionMatrix(double d);


    @WrapOperation(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;)V"
            )
    )
    private void setupCameraWithMountedShip(final LevelRenderer instance, final PoseStack ignore, final Vec3 vec3,
                                            final Matrix4f matrix4f, final Operation<Void> original, final float partialTicks,
                                            final long finishTimeNano, final PoseStack matrixStack) {

        final ClientLevel clientLevel = minecraft.level;
        final Entity player = minecraft.player;
        if (clientLevel == null || player == null) {
            original.call(instance, matrixStack, vec3, matrix4f);
            return;
        }

        final QueryableShipData<LoadedShip> ships = VSGameUtilsKt.getShipObjectWorld(player.level()).getLoadedShips();

        Ship ship = null;
        Vector3d pos = VectorConversionsMCKt.toJOML(player.getPosition(partialTicks));

        for (Ship it: ships) {
            if (it.getWorldAABB().containsPoint(pos)) {
                ship = it;
                break;
            }
        }

        if (ship == null) {
            original.call(instance, matrixStack, vec3, matrix4f);
            return;
        }

        final Entity playerVehicle = player.getVehicle();
        if (playerVehicle != null) {
            original.call(instance, matrixStack, vec3, matrix4f);
            return;
        }

        // Update [matrixStack] to mount the camera to the ship

        final Camera camera = this.mainCamera;
        if (camera == null) {
            original.call(instance, matrixStack, vec3, matrix4f);
            return;
        }


        final ClientShip clientShip = (ClientShip) ship;

        final boolean isFirstPerson = minecraft.options.getCameraType().isFirstPerson();

        if (!isFirstPerson) {
            ((IVSCamera) camera).setupWithShipMounted(
                    this.minecraft.level,
                    this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity(),
                    !this.minecraft.options.getCameraType().isFirstPerson(),
                    this.minecraft.options.getCameraType().isMirrored(),
                    partialTicks,
                    clientShip,
                    clientShip.getRenderTransform().getWorldToShip().transformPosition(pos)
            );
        }

        // Apply the ship render transform to [matrixStack]
        final Quaternionf invShipRenderRotation = new Quaternionf(
                clientShip.getRenderTransform().getShipToWorldRotation().conjugate(new Quaterniond())
        );
        matrixStack.mulPose(invShipRenderRotation);

        // We also need to recompute [inverseViewRotationMatrix] after updating [matrixStack]
        {
            final Matrix3f matrix3f = new Matrix3f(matrixStack.last().normal());
            matrix3f.invert();
            RenderSystem.setInverseViewRotationMatrix(matrix3f);
        }

        final double fov = this.getFov(camera, partialTicks, true);

        if (isFirstPerson) {
            original.call(instance, matrixStack, vec3,
                    this.getProjectionMatrix(Math.max(fov, this.minecraft.options.fov().get())));
        } else {
            original.call(instance, matrixStack, camera.getPosition(),
                    this.getProjectionMatrix(Math.max(fov, this.minecraft.options.fov().get())));
        }
    }
}
