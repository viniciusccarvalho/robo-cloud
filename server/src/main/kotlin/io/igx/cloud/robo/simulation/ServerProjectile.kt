package io.igx.cloud.robo.simulation

import io.igx.cloud.robo.Movable
import io.igx.cloud.robo.proto.Projectile
import org.jbox2d.common.Vec2

/**
 * @author vinicius
 * Represents a "live" projectile, trajectory is updated on every frame
 */
class ServerProjectile(var bearing: Float = 0.0f, var center: Vec2 = Vec2(0.0f, 0.0f)) : Movable {

    val speed = 30.0f/1_000 // pixels per second

    override fun updateCoordinates(delta: Long) {
        center = center.moveTo(bearing, speed*delta)
    }

    fun getState() : Projectile {
        return Projectile.newBuilder()

                .build()
    }

}