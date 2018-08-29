package io.igx.cloud.robo.simulation

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.Movable
import io.igx.cloud.robo.proto.*
import mu.KotlinLogging
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.jbox2d.dynamics.Body
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Vinicius Carvalho
 *
 * The ServerRobot is responsible to update the internal state represented via the Robot entity.
 * Internal state is queued upon receive of Action stream events, the ArenaService will request an update
 * to the internal state and any perceived event is then sent back to the clients
 */
class ServerRobot(val outgoing: StreamObserver<FrameUpdate>, val body: Body, val worldConfig: WorldConfig = WorldConfig()) : Movable {

    var acceleration = 0.0
    val speed = 20.0 / 1_000 // pixels per second
    var health = 3
    var projectiles = 1
    var score = 0
    val id = UUID.randomUUID().toString()
    val events = LinkedList<Any>()
    val radar: Radar
    private val connected = AtomicBoolean(false)

    init {
        val range = Math.sqrt(Math.pow(worldConfig.screen.width.toDouble(), 2.0) + Math.pow(worldConfig.screen.height.toDouble(), 2.0)) * 1.2
        radar = Radar(Vector2D(body.position.x.toDouble(), body.position.y.toDouble()), body.angle.toDouble(), range)
    }

    private val logger = KotlinLogging.logger {}

    fun subscribe(): StreamObserver<Action> {
        connected.set(true)
        return object : StreamObserver<Action> {

            override fun onNext(value: Action?) {
                onActionUpdate(value!!)
            }

            override fun onError(t: Throwable?) {

            }

            override fun onCompleted() {
                try {
                    outgoing.onCompleted()
                } catch (e: Exception) {

                } finally {
                    disconnect()
                }
            }

        }
    }

    fun broadcast() {
        val timestamp = System.currentTimeMillis()
        val robot = getState()
        if (connected.get()) {
            try {
                outgoing.onNext(FrameUpdate.newBuilder()
                        .setTimestamp(timestamp)
                        .setRobotState(robot)
                        .setEventType(EventType.STATUS_UPDATED)
                        .build())
                while (events.isNotEmpty()) {
                    val event = events.pop()
                    val builder = FrameUpdate.newBuilder()
                    builder.robotState = robot
                    builder.timestamp = timestamp
                    setEvent(builder, event)
                    val frameUpdate = builder.build()
                    outgoing.onNext(frameUpdate)
                }
            } catch (e: Exception) {
               disconnect()
            }
        }
    }

    override fun updateCoordinates(delta: Long) {
        radar.update(Vector2D(body.position.x.toDouble(), body.position.y.toDouble()), body.angle.toDouble())
        if (isFiring()) {
        }
    }

    fun disconnect(){
       connected.set(false)
    }

    fun isConnected() : Boolean = connected.get()

    fun scanTarget(target: ServerRobot) {
        if (radar.contains(Vector2D(body.position.x.toDouble(), body.position.y.toDouble()))) {
            events.add(EnemyDetectedEvent.newBuilder()
                    .setTimestamp(System.currentTimeMillis())
                    .setTarget(target.getState())
                    .build())
        }
    }


    private fun setEvent(builder: FrameUpdate.Builder, event: Any) {
        val type = findEventType(event)
        builder.eventType = type
        when (type) {
            EventType.ENEMY_DETECTED -> builder.enemyDetectedEvent = event as EnemyDetectedEvent
            EventType.HIT_ENEMY -> builder.hitEnemyEvent = event as HitEnemyEvent
            EventType.DESTROYED -> builder.destroyedEvent = event as DestroyedEvent
            EventType.COLISION_DETECTED -> builder.collisionDetectedEvent = event as CollisionDetectedEvent
            EventType.HIT_BY -> builder.hitByEvent = event as HitByEvent
            EventType.STARTED -> builder.startedEvent = event as StartedEvent
        }

    }

    private fun findEventType(type: Any): EventType {
        return when (type.javaClass) {

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

    fun getState(): Robot {
        return Robot.newBuilder()
                .setId(id)
                .setSpeed(speed)
                .setHealth(health)
                .setScore(score)
                .setProjectiles(projectiles)
                .setBox(Box.newBuilder()
                        .setBearing(this.body.angle)
                        .setHeight(this.worldConfig.botBox.height)
                        .setWidth(this.worldConfig.botBox.width)
                        .setCoordinates(Coordinates.newBuilder()
                                .setX(this.body.position.x)
                                .setY(this.body.position.y)
                                .build())
                        .build())
                .build()
    }

    private fun onActionUpdate(action: Action) {
        when (action.actionType) {
            ActionType.THROTTLE -> this.acceleration = normalize(action.value)

            ActionType.FIRE -> "fire"

            ActionType.JOIN -> "join"

            ActionType.LEAVE -> "leave"

            ActionType.ROTATE -> this.body.angularVelocity = 0.5f
        }
    }

    private fun normalize(value: Double): Double = when {
        value < 0.0 -> Math.max(value, -1.0)
        value > 0.0 -> Math.min(value, 1.0)
        else -> 0.0
    }

    private fun isFiring(): Boolean {
        return projectiles == 0
    }


}




