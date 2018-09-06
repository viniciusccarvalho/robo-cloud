package io.igx.cloud.robo.simulation

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.Movable
import io.igx.cloud.robo.proto.*
import io.igx.cloud.robo.proto.Robot
import mu.KotlinLogging
import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2
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
class ServerRobot(val id: String = UUID.randomUUID().toString(), val outgoing: StreamObserver<FrameUpdate>, val body: Body, val callback: ArenaCallback) : Movable {

    val events = LinkedList<Any>()
    val radar: Radar
    val name = RobotNameFactory.getName()
    private val connected = AtomicBoolean(false)
    private val helper = GameHelper
    var direction = 0.0f
    var rotation = 0.0f
    var health = 100
    var projectiles = helper.engineConfig.botConfig.maxProjectiles
    var score = 0
    var coolDown = 0

    init {

        val range = MathUtils.sqrt((helper.worldWidth*helper.worldWidth) + (helper.worldHeight*helper.worldHeight))
        radar = Radar(Vec2(body.position.x, body.position.y), body.angle, range)
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

        if (connected.get()) {
            try {
                val robot = getState()
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
        this.coolDown = Math.max(--coolDown, 0)
        radar.update(Vec2(body.position.x, body.position.y), body.angle)
    }

    fun disconnect() {
        connected.set(false)
    }

    fun isConnected(): Boolean = connected.get()

    fun onRayCast(target: ServerRobot){
        events.add(EnemyDetectedEvent.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setTarget(target.getState())
                .build())
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
            else -> logger.warn { "Invalid state" }
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
                .setSpeed(helper.engineConfig.botConfig.speed)
                .setHealth(health)
                .setScore(score)
                .setName(name)
                .setProjectiles(projectiles)
                .setBox(io.igx.cloud.robo.proto.Box.newBuilder()
                        .setBearing(this.body.angle)
                        .setHeight(helper.engineConfig.botConfig.box.height)
                        .setWidth(helper.engineConfig.botConfig.box.width)
                        .setCoordinates(io.igx.cloud.robo.proto.Coordinates.newBuilder()
                                .setX(this.body.position.x)
                                .setY(this.body.position.y)
                                .build())
                        .build())
                .build()
    }

    fun reload(){
        synchronized(this) {
            this.projectiles = Math.min(++this.projectiles, helper.engineConfig.botConfig.maxProjectiles)
            this.coolDown = helper.engineConfig.fps / 2
        }
    }

    fun hitBy(source: ServerRobot){
        synchronized(this) {
            this.health = Math.max(0, this.health - helper.engineConfig.simulationConfig.hitDamage)
            this.score -= helper.engineConfig.simulationConfig.hitDamage
            events.add(HitByEvent.newBuilder()
                    .setTimestamp(System.currentTimeMillis())
                    .setSource(source.getState())
                    .build())
        }
    }

    fun hitEnemy(target: ServerRobot?) {
        synchronized(this){
            this.score += helper.engineConfig.simulationConfig.hitScore
            events.add(HitEnemyEvent.newBuilder()
                    .setTimestamp(System.currentTimeMillis())
                    .setTarget(target?.getState())
                    .build())
        }
    }

    private fun onActionUpdate(action: Action) {
        when (action.actionType) {
            ActionType.THROTTLE -> {
                this.direction = normalize(action.value)
                val x = 1.0f * MathUtils.cos(body.angle) * direction * helper.engineConfig.botConfig.speed
                val y = 1.0f * MathUtils.sin(body.angle) * direction * helper.engineConfig.botConfig.speed
                this.body.linearVelocity = Vec2(x, y)

            }

            ActionType.FIRE -> {
                synchronized(this.projectiles){
                    if(this.projectiles > 0 && this.coolDown == 0){
                        this.projectiles--
                        this.coolDown = helper.engineConfig.fps
                        callback.onFireEvent(this)
                    }
                }
            }

            ActionType.JOIN -> "join"

            ActionType.LEAVE -> "leave"

            ActionType.ROTATE -> {
                this.rotation = normalize(action.value) * 0.4f
                this.body.angularVelocity = this.rotation
            }
            else -> logger.warn { "Invalid state" }
        }
    }

    private fun normalize(value: Float): Float = when {
        value < 0.0 -> Math.max(value, -1.0f)
        value > 0.0 -> Math.min(value, 1.0f)
        else -> 0.0f
    }

    fun isAlive() : Boolean {
        synchronized(this.health){
            return this.health > 0
        }
    }


}
