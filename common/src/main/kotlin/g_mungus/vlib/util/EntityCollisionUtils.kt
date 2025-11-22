package g_mungus.vlib.util

import g_mungus.vlib.dimension.DimensionSettingsManager
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.impl.shadow.Dk.createPolygonFromAABB
import org.valkyrienskies.core.internal.collision.VsiConvexPolygonc
import org.valkyrienskies.core.internal.collision.VsiEntityPolygonCollider
import org.valkyrienskies.core.util.extend
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toMinecraft
import org.valkyrienskies.mod.util.BugFixUtil
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun adjustEntityMovementForShipCollisions(
    entity: Entity?,
    movement: Vec3,
    entityBoundingBox: AABB,
    world: Level,
    collider: VsiEntityPolygonCollider
): Vec3 {
    // Inflate the bounding box more for players than other entities, to give players a better collision result.
    // Note that this increases the cost of doing collision, so we only do it for the players
    val inflation = if (entity is Player) 0.5 else 0.1
    val stepHeight: Double = entity?.maxUpStep()?.toDouble() ?: 0.0
    // Add [max(stepHeight - inflation, 0.0)] to search for polygons we might collide with while stepping
    val collidingShipPolygons =
        getShipPolygonsCollidingWithEntity(
            entity, Vec3(movement.x(), movement.y() + max(stepHeight - inflation, 0.0), movement.z()),
            entityBoundingBox.inflate(inflation), world, collider
        )

    if (collidingShipPolygons.isEmpty()) {
        return movement
    }

    if (entity != null) {
        val collidingWith = (entity as IEntityDraggingInformationProvider).draggingInformation.lastShipStoodOn


        if (collidingWith != null) {

            if (entity is Player) {
                val shipToWorldRotation: Quaterniondc? =
                    world.shipObjectWorld.loadedShips.getById(collidingWith)?.transform?.shipToWorldRotation
                if (shipToWorldRotation != null) {
                    val (newMovement, shipCollidingWith) = collider.adjustEntityMovementForPolygonCollisions(
                        movement.toJOML(),
                        shrinkAABBXZ(entityBoundingBox.toJOML(), shipToWorldRotation),
                        stepHeight,
                        collidingShipPolygons
                    )
                    if (shipCollidingWith != null) {
                        (entity as IEntityDraggingInformationProvider).draggingInformation.lastShipStoodOn = shipCollidingWith
                    }
                    return newMovement.toMinecraft()
                }
            }
        }
    }

    val (newMovement, shipCollidingWith) = collider.adjustEntityMovementForPolygonCollisions(
        movement.toJOML(), entityBoundingBox.toJOML(), stepHeight, collidingShipPolygons
    )

    if (entity != null && shipCollidingWith != null) {
        (entity as IEntityDraggingInformationProvider).draggingInformation.lastShipStoodOn = shipCollidingWith
    }

    return newMovement.toMinecraft()
}

fun getShipPolygonsCollidingWithEntity(
    entity: Entity?,
    movement: Vec3,
    entityBoundingBox: AABB,
    world: Level,
    collider: VsiEntityPolygonCollider
): List<VsiConvexPolygonc> {
    val entityBoxWithMovement = entityBoundingBox.expandTowards(movement)
    val collidingPolygons: MutableList<VsiConvexPolygonc> = ArrayList()
    val entityBoundingBoxExtended = entityBoundingBox.toJOML().extend(movement.toJOML())

    val dimSettings = DimensionSettingsManager.getSettingsForLevel(world.dimension().location())
    val shouldApplyFunkyFix = dimSettings.shipScale == 0.0625 && dimSettings.gravity == 0.0

    for (shipObject in world.shipObjectWorld.loadedShips.getIntersecting(entityBoundingBoxExtended)) {
        val shipTransform = shipObject.transform
        val entityPolyInShipCoordinates: VsiConvexPolygonc = collider.createPolygonFromAABB(
            entityBoxWithMovement.toJOML(),
            shipTransform.worldToShip
        )
        var entityBoundingBoxInShipCoordinates: AABBdc = entityPolyInShipCoordinates.getEnclosingAABB(AABBd())

        if (shouldApplyFunkyFix) {
            val worldAABB = entityBoxWithMovement.toJOML()
            val originalHalfExtents = Vector3d(
                (worldAABB.maxX - worldAABB.minX) / 2.0,
                (worldAABB.maxY - worldAABB.minY) / 2.0,
                (worldAABB.maxZ - worldAABB.minZ) / 2.0
            )
            originalHalfExtents.mul(2.0)

            val center = Vector3d().apply { entityBoundingBoxInShipCoordinates.center(this) }
            entityBoundingBoxInShipCoordinates = AABBd(
                center.x - originalHalfExtents.x, center.y - originalHalfExtents.y, center.z - originalHalfExtents.z,
                center.x + originalHalfExtents.x, center.y + originalHalfExtents.y, center.z + originalHalfExtents.z
            )
        }

        if (entity is Player) entityBoundingBoxInShipCoordinates = shrinkAABBXZ(entityBoundingBoxInShipCoordinates, shipTransform.shipToWorldRotation)
        if (BugFixUtil.isCollisionBoxTooBig(entityBoundingBoxInShipCoordinates.toMinecraft())) {
            // Box too large, skip it
            continue
        }
        val shipBlockCollisionStream =
            world.getBlockCollisions(entity, entityBoundingBoxInShipCoordinates.toMinecraft())
        shipBlockCollisionStream.forEach { voxelShape: VoxelShape ->
            voxelShape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
                val shipPolygon: VsiConvexPolygonc = createPolygonFromAABB(
                    AABBd(minX, minY, minZ, maxX, maxY, maxZ),
                    shipTransform.shipToWorld,
                    shipObject.id
                )
                collidingPolygons.add(shipPolygon)
            }
        }
    }
    return collidingPolygons
}

private fun shrinkAABBXZ(aabb: AABBdc, shipToWorldRotation: Quaterniondc): AABBdc {
    val center = Vector3d().apply { aabb.center(this) }

    val halfExtents = Vector3d().apply { aabb.extent(this) }

    val yaw = shipToWorldRotation.getEulerAnglesXYZ(Vector3d()).y
    val scale = 1.0 / (abs(cos(yaw)) + abs(sin(yaw)))

    halfExtents.x *= scale
    halfExtents.z *= scale

    val newMin = Vector3d(center.x - halfExtents.x, aabb.minY(), center.z - halfExtents.z)
    val newMax = Vector3d(center.x + halfExtents.x, aabb.maxY(), center.z + halfExtents.z)

    return(AABBd(newMin, newMax))
}
