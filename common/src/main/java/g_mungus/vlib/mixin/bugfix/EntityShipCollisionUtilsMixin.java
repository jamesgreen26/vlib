package g_mungus.vlib.mixin.bugfix;

import g_mungus.vlib.util.EntityCollisionUtilsKt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.internal.collision.VsiEntityPolygonCollider;
import org.valkyrienskies.mod.common.util.EntityShipCollisionUtils;

import java.util.List;

/**
 * @author G_Mungus
 *
 * Reason: shrink player hitbox on xz plane while colliding with ships according to their y rotation
 *         to simulate the collision that would happen with axis-aligned ships
 **/

@Mixin(EntityShipCollisionUtils.class)
public class EntityShipCollisionUtilsMixin {

    @Final
    @Shadow(remap = false)
    private static VsiEntityPolygonCollider collider;


    @Inject(method = "adjustEntityMovementForShipCollisions", at = @At("HEAD"), cancellable = true, remap = false)
    private void adjustEntityMovementForShipCollisionsMixin(Entity entity, Vec3 movement, AABB entityBoundingBox, Level world, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(EntityCollisionUtilsKt.adjustEntityMovementForShipCollisions(entity, movement, entityBoundingBox, world, collider));
    }
}
