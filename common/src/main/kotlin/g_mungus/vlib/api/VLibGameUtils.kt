package g_mungus.vlib.api

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import org.valkyrienskies.mod.common.VSClientGameUtils
import org.valkyrienskies.mod.common.getShipManagingPos
import g_mungus.vlib.VLib.LOGGER
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.mod.common.allShips
import org.valkyrienskies.mod.common.dimensionId


fun saveShipToTemplate(structurePath: String, level: ServerLevel, blockPos: BlockPos) {
    try {
        saveShipToTemplate(structurePath, level, level.getShipManagingPos(blockPos)!!.id)
    } catch(e: Exception) {
        LOGGER.error("Could not find ship for blockPos: $blockPos")
    }
}

fun saveShipToTemplate(structurePath: String, level: ServerLevel, shipId: Long) {
    val ship = level.allShips.getById(shipId)
    if (ship == null) {
        LOGGER.error("Could not find ship id: $shipId in world: ${level.dimensionId}")
        return
    }

    LOGGER.info(ship.shipAABB.toString())
}