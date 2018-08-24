package io.igx.cloud.robo

import io.grpc.stub.StreamObserver
import mu.KotlinLogging
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import java.util.*

/**
 * @author vinicius
 * The ServerRobot is responsible to update the internal state represented via the Robot entity.
 * Internal state is queued upon receive of Action stream events, the ArenaService will request an update
 * to the internal state and any perceived event is then sent back to the clients
 */
class ServerRobot(val outgoing: StreamObserver<FrameUpdate>, var center: Vector2D = Vector2D(0.0, 0.0), var bearing: Double = 0.0) : Movable{

    var rotationDirection = 0.0
    var acceleration = 0.0
    val rotationSpeed = 30.0/1_000 //degrees per second
    val speed = 20.0/1_000 // pixels per second
    var health = 3
    var projectiles = 1
    var score = 0
    val id = UUID.randomUUID().toString()
    val events = LinkedList<Any>()
    var projectile = ServerProjectile(bearing, center)
    val radar: Radar

    init {
        radar = Radar(center, bearing)
    }

    private val logger = KotlinLogging.logger {}

    fun subscribe(): StreamObserver<Action> =
            object : StreamObserver<Action> {

                override fun onNext(value: Action?) {
                    onActionUpdate(value!!)
                }

                override fun onError(t: Throwable?) {

                }

                override fun onCompleted() {

                }

            }

    fun broadcast() {
        val timestamp = System.currentTimeMillis()
        val robot = Robot.newBuilder()
                .setId(id)
                .setSpeed(speed)
                .setBearing(bearing)
                .setHealth(health)
                .setScore(score)
                .setProjectiles(projectiles)
                .setCoordinates(Coordinates.newBuilder().setX(center.x).setY(center.y).build())
                .build()
        if(events.isEmpty()){
            outgoing.onNext(FrameUpdate.newBuilder()
                    .setTimestamp(timestamp)
                    .setRobotState(robot)
                    .setEventType(EventType.STATUS_UPDATED)
                    .build())
        }
        for(event in events) {
            val builder = FrameUpdate.newBuilder()
            builder.robotState = robot
            builder.timestamp = timestamp
            setEvent(builder, event)
            outgoing.onNext(builder.build())
        }

    }

    override fun updateCoordinates(delta: Long) {
        bearing += rotationDirection * rotationSpeed * delta
        center = center.moveTo(Math.toRadians(bearing), delta * speed)
        radar.update(center, bearing)
        if(isFiring()){
            projectile.updateCoordinates(delta)
        }
    }


    private fun setEvent(builder: FrameUpdate.Builder, event : Any) {
        val type = findEventType(event)
        when(type) {
            EventType.ENEMY_DETECTED -> builder.enemyDetectedEvent = event as EnemyDetectedEvent
            EventType.HIT_ENEMY -> builder.hitEnemyEvent = event as HitEnemyEvent
            EventType.DESTROYED -> builder.destroyedEvent = event as DestroyedEvent
            EventType.COLISION_DETECTED -> builder.collisionDetectedEvent = event as CollisionDetectedEvent
            EventType.HIT_BY -> builder.hitByEvent = event as HitByEvent
            EventType.STARTED -> builder.startedEvent = event as StartedEvent
        }
    }

    private fun findEventType(type: Any) : EventType {
        return when(Any::class.java){

            EnemyDetectedEvent::class.java -> EventType.ENEMY_DETECTED
            HitByEvent::class.java -> EventType.HIT_BY
            HitEnemyEvent::class.java -> EventType.HIT_ENEMY
            CollisionDetectedEvent::class.java -> EventType.COLISION_DETECTED
            DestroyedEvent::class.java -> EventType.DESTROYED
            StartedEvent::class.java -> EventType.STARTED

            else -> {
                EventType.UNRECOGNIZED
            }
        }
    }

    private fun onActionUpdate(action: Action) {
        when (action.actionType) {
            ActionType.THROTTLE -> this.acceleration = normalize(action.value)

            ActionType.FIRE -> "fire"

            ActionType.JOIN -> "join"

            ActionType.LEAVE -> "leave"

            ActionType.ROTATE -> this.rotationDirection = normalize(action.value)
        }
    }

    private fun normalize(value: Double) : Double = when {
        value < 0.0 -> Math.max(value, -1.0)
        value > 0.0 -> Math.min(value, 1.0)
        else -> 0.0
    }

    private fun isFiring() : Boolean {
        return  projectiles == 0
    }

}


/**
 * The Radar is a projection triangle from the center of the robot towards the end of the screen. It has a ten degree angle between the projection lines, and it's used to find if another robot center is
 * contained within this projection. It's based on Ray Casting algorithms to find if a point is cointained inside a polygon
 */
class Radar(var center: Vector2D, var bearing: Double, var range: Double = 1000.0) {
    var points : Array<Vector2D> = Array(3) { _ -> Vector2D(0.0, 0.0) }

    init {
       update(center, bearing)
    }

    fun update(center: Vector2D, bearing: Double){
        points[0] = center
        points[1] = center.moveTo(Math.toRadians(bearing+5), range)
        points[2] = center.moveTo(Math.toRadians(bearing-5), range)
    }

    fun contains(other: Vector2D) : Boolean{
        var contains = false
        var i = 0
        var j = points.size - 1
        while (i < points.size) {
            if (points[i].y > other.y != points[j].y > other.y && other.x < (points[j].x - points[i].x) * (other.y - points[i].y) / (points[j].y - points[i].y) + points[i].x) {
                contains = !contains
            }
            j = i++
        }
        return contains
    }
}

/**
 * Moves a point in space considering the distance and angle
 */
fun Vector2D.moveTo(angle: Double, distance: Double)  = Vector2D(this.x + (Math.cos(angle) * distance), this.y + (Math.sin(angle)*distance) )

