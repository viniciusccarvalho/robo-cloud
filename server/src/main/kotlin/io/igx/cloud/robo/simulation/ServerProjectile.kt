package io.igx.cloud.robo.simulation

import io.igx.cloud.robo.proto.Box
import io.igx.cloud.robo.proto.Projectile
import io.igx.cloud.robo.proto.Coordinates
import org.jbox2d.dynamics.Body

/**
 * @author vinicius
 * Represents a "live" projectile, trajectory is updated on every frame
 */
class ServerProjectile(val body: Body, val id: String) {

    fun getState() : Projectile {
        return Projectile.newBuilder()
                .setRobotId(getRobotId())
                .setBox(Box.newBuilder()
                        .setBearing(body.angle)
                        .setCoordinates(Coordinates.newBuilder()
                                .setX(body.position.x)
                                .setY(body.position.y)
                                .build())
                        .build())
                .build()
    }

    fun getRobotId() : String {
        val data = body.userData as BodyData
        return data.context["robotId"].orEmpty()
    }

}