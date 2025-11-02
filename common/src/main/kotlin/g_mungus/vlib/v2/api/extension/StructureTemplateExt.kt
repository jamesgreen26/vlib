package g_mungus.vlib.v2.api.extension

import g_mungus.vlib.dimension.DimensionSettingsManager
import g_mungus.vlib.v2.api.HasSpecialSaveBehavior
import g_mungus.vlib.v2.impl.assembly.BoundedVoxelSet
import g_mungus.vlib.v2.util.injected.CanFillFromVoxelSet
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.toBlockPos
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.yRange

fun StructureTemplate.placeAsShip(level: ServerLevel, blockPos: BlockPos, static: Boolean = false): ServerShip {
    val dimensionSettings = DimensionSettingsManager.getSettingsForLevel(level.dimensionId)

    val ship = level.shipObjectWorld.createNewShipAtBlock(blockPos.toJOML(), false, dimensionSettings.shipScale, level.dimensionId)
    ship.isStatic = true

    val centreOfShip = ship.chunkClaim.getCenterBlockCoordinates(level.yRange, Vector3i()).toBlockPos().atY(blockPos.y)

    val structurePlaceSettings = StructurePlaceSettings()
    structurePlaceSettings.rotationPivot = centreOfShip

    placeInWorld(
        level,
        centreOfShip,
        centreOfShip,
        structurePlaceSettings,
        level.random,
        2
    )

    val newPos: Vector3d = ship.transform.positionInWorld.add(ship.inertiaData.centerOfMassInShip, Vector3d()).sub(centreOfShip.x.toDouble(), centreOfShip.y.toDouble(), centreOfShip.z.toDouble())

    level.shipObjectWorld.teleportShip(ship, ShipTeleportDataImpl(newPos = newPos))

    ship.forEachBlock {
        val blockEntity = level.getBlockEntity(it)

        if (blockEntity is HasSpecialSaveBehavior) blockEntity.executeAfterLoadingShip()
    }

    ship.isStatic = static
    return ship
}

fun StructureTemplate.fillFromVoxelSet(level: ServerLevel, voxelSet: BoundedVoxelSet) = (this as CanFillFromVoxelSet).`vlib$fillFromVoxelSet`(level, voxelSet)
