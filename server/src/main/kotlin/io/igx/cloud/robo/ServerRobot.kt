package io.igx.cloud.robo

/**
 * @author vinicius
 * The ServerRobot is responsible to update the internal state represented via the Robot entity.
 * Internal state is queued upon receive of Action stream events, the ArenaService will request an update
 * to the internal state and any perceived event is then sent back to the clients
 */
class ServerRobot {
    val projectile = ServerProjectile()
}