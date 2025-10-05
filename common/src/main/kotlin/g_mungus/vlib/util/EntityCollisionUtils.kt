package g_mungus.vlib.util

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
import org.valkyrienskies.core.apigame.collision.ConvexPolygonc
import org.valkyrienskies.core.apigame.collision.EntityPolygonCollider
import org.valkyrienskies.core.impl.collision.k.createPolygonFromAABB
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
    collider: EntityPolygonCollider
): Vec3 {
    // Inflate the bounding box more for players than other entities, to give players a better collision result.
    // Note that this increases the cost of doing collision, so we only do it for the players
    val inflation = if (entity is Player) 0.5 else 0.1
    val stepHeight: Double = entity?.maxUpStep()?.toDouble() ?: 0.0
    // Add [max(stepHeight - inflation, 0.0)] to search for polygons we might collide with while stepping
    val collidingShipPolygonsWithRotation = getShipPolygonsCollidingWithEntityWithRotation(
            entity, Vec3(movement.x(), movement.y() + max(stepHeight - inflation, 0.0), movement.z()),
            entityBoundingBox.inflate(inflation), world, collider
        )

    if (collidingShipPolygonsWithRotation.isEmpty()) {
        return movement
    }

    val shipRotation = collidingShipPolygonsWithRotation.first().second
    val shipRotationInverse = org.joml.Quaterniond(shipRotation).conjugate()

    val rotatedMovement = shipRotationInverse.transform(movement.toJOML())


    val (newMovement, shipCollidingWith) = collider.adjustEntityMovementForPolygonCollisions(
        rotatedMovement, entityBoundingBox.toJOML(), stepHeight, collidingShipPolygonsWithRotation.map { it.first }
    )

    // Rotate movement back to world space
    val finalMovement = shipRotation.transform(Vector3d(newMovement))

    if (entity != null) {
        if (shipCollidingWith != null) {
            // Update the [IEntity.lastShipStoodOn]
            (entity as IEntityDraggingInformationProvider).draggingInformation.lastShipStoodOn = shipCollidingWith
        }
    }
    return finalMovement.toMinecraft()
}

fun getShipPolygonsCollidingWithEntityWithRotation(
    entity: Entity?,
    movement: Vec3,
    entityBoundingBox: AABB,
    world: Level,
    collider: EntityPolygonCollider
): List<Pair<ConvexPolygonc, org.joml.Quaterniond>> {
    val entityBoxWithMovement = entityBoundingBox.expandTowards(movement)
    val collidingPolygons: MutableList<Pair<ConvexPolygonc, org.joml.Quaterniond>> = ArrayList()
    val entityBoundingBoxExtended = entityBoundingBox.toJOML().extend(movement.toJOML())
    for (shipObject in world.shipObjectWorld.loadedShips.getIntersecting(entityBoundingBoxExtended)) {
        val shipTransform = shipObject.transform
        val shipRotation = org.joml.Quaterniond(shipTransform.shipToWorldRotation)

        // Calculate the center of the entity box in world space
        val boxCenterWorld = Vector3d(
            (entityBoxWithMovement.minX + entityBoxWithMovement.maxX) / 2.0,
            (entityBoxWithMovement.minY + entityBoxWithMovement.maxY) / 2.0,
            (entityBoxWithMovement.minZ + entityBoxWithMovement.maxZ) / 2.0
        )


        // We need to: rotate the entity box to align with ship axes, THEN apply worldToShip
        // This means: worldToShip * rotateAroundCenter(shipToWorldRotation)
        // Which is: worldToShip * translate(center) * rotate(shipToWorldRotation) * translate(-center)
        val modifiedWorldToShip = org.joml.Matrix4d(shipTransform.worldToShip)
            .mul(
                org.joml.Matrix4d()
                    .translate(boxCenterWorld)
                    .rotate(shipTransform.shipToWorldRotation)
                    .translate(-boxCenterWorld.x, -boxCenterWorld.y, -boxCenterWorld.z)
            )


        val entityPolyInShipCoordinates: ConvexPolygonc = collider.createPolygonFromAABB(
            entityBoxWithMovement.toJOML(),
            modifiedWorldToShip
        )
        val entityBoundingBoxInShipCoordinates: AABBdc = entityPolyInShipCoordinates.getEnclosingAABB(AABBd())


        if (BugFixUtil.isCollisionBoxToBig(entityBoundingBoxInShipCoordinates.toMinecraft())) {
            // Box too large, skip it
            continue
        }
        val shipBlockCollisionStream =
            world.getBlockCollisions(entity, entityBoundingBoxInShipCoordinates.toMinecraft())

        // For shipToWorld, we need the inverse: rotate back to world alignment around the center
        // This means: rotateAroundCenter(shipToWorldRotation^-1) * shipToWorld
        val shipToWorldRotationInverse = org.joml.Quaterniond(shipTransform.shipToWorldRotation).conjugate()
        val modifiedShipToWorld = org.joml.Matrix4d()
            .translate(boxCenterWorld)
            .rotate(shipToWorldRotationInverse)
            .translate(-boxCenterWorld.x, -boxCenterWorld.y, -boxCenterWorld.z)
            .mul(shipTransform.shipToWorld)


        shipBlockCollisionStream.forEach { voxelShape: VoxelShape ->
            voxelShape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
                val shipPolygon: ConvexPolygonc = createPolygonFromAABB(
                    AABBd(minX, minY, minZ, maxX, maxY, maxZ),
                    modifiedShipToWorld,
                    shipObject.id
                )
                collidingPolygons.add(Pair(shipPolygon, shipRotation))
            }
        }
    }
    return collidingPolygons
}

fun getShipPolygonsCollidingWithEntity(
    entity: Entity?,
    movement: Vec3,
    entityBoundingBox: AABB,
    world: Level,
    collider: EntityPolygonCollider
): List<ConvexPolygonc> {
    return getShipPolygonsCollidingWithEntityWithRotation(entity, movement, entityBoundingBox, world, collider)
        .map { it.first }
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