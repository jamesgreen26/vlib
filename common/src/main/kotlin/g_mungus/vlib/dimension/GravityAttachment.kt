package g_mungus.vlib.dimension

import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.ShipForcesInducer
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl

class GravityAttachment(val dimension: String) : ShipForcesInducer {

    companion object {

        fun getOrCreate(ship: ServerShip): GravityAttachment {
            var attachment = ship.getAttachment(GravityAttachment::class.java)
            if (attachment == null) {
                attachment = GravityAttachment(ship.chunkClaimDimension)
                ship.saveAttachment(GravityAttachment::class.java, attachment)
            }
            return attachment
        }
    }

    override fun applyForces(physShip: PhysShip) {


        val dimensionSettings = DimensionSettingsManager.getSettingsForLevel(dimension)

        if (dimensionSettings.gravity != 1.0 && dimensionSettings.shouldApplyGravity) {
            val gravity = (1 - dimensionSettings.gravity) * 10 * (physShip as PhysShipImpl).inertia.shipMass

            try {
                physShip.applyInvariantForce(Vector3d(0.0, gravity, 0.0))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}