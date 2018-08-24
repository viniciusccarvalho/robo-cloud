package io.igx.cloud.robo

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

/**
 * @author vinicius
 * Represents a "live" projectile, trajectory is updated on every frame
 */
class ServerProjectile(var bearing: Double = 0.0, var center: Vector2D = Vector2D(0.0, 0.0)) : Movable {

    val speed = 30.0/1_000 // pixels per second

    override fun updateCoordinates(delta: Long) {

        center = center.moveTo(Math.toRadians(bearing), speed*delta)
    }

}