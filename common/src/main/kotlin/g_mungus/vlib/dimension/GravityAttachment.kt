package g_mungus.vlib.dimension

import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.ShipPhysicsListener
import org.valkyrienskies.core.api.world.PhysLevel
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl

class GravityAttachment(dimension: String) : ShipPhysicsListener {
    var dimension = dimension
        private set

    companion object {

        fun getOrCreate(ship: LoadedServerShip): GravityAttachment {
            var attachment = ship.getAttachment(GravityAttachment::class.java)
            if (attachment == null) {
                attachment = GravityAttachment(ship.chunkClaimDimension)
                ship.setAttachment(attachment)
            } else {
                attachment.dimension = ship.chunkClaimDimension
            }
            return attachment
        }
    }

    override fun physTick(physShip: PhysShip, physLevel: PhysLevel) {
        val dimensionSettings = DimensionSettingsManager.getSettingsForLevel(dimension)

        if (dimensionSettings.gravity != 1.0 && dimensionSettings.shouldApplyGravity) {
            val gravity = (1 - dimensionSettings.gravity) * 10 * (physShip as PhysShipImpl).mass

            try {
                physShip.applyInvariantForce(Vector3d(0.0, gravity, 0.0))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
