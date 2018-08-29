package io.igx.cloud.robo.services

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.proto.FrameUpdate
import io.igx.cloud.robo.simulation.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author vinicius
 * The ArenaService is responsible to maintain the state of a match, update the frames,
 * detect collisions.
 */
class ArenaService(val config: WorldConfig = WorldConfig(), val id: String = UUID.randomUUID().toString()) {

    private var activeRobots = mutableListOf<ServerRobot>()
    private var destroyedRobots = mutableListOf<ServerRobot>()
    private var projectiles = mutableListOf<ServerProjectile>()
    private var robotLock = ReentrantLock()
    private var watchers = mutableListOf<Channel<ArenaView>>()
    val FPS = 30
    val ticks = 1000 / FPS
    var running = AtomicBoolean(false)
    var delta = 0L
    private var currentTime = System.currentTimeMillis()
    private val random = Random()
    private lateinit var job: Job
    lateinit var state: ArenaState
    private val logger = KotlinLogging.logger {}
    private val world: World = World(Vec2())
    private val fixtureDef: FixtureDef = FixtureDef()

    init {
        val shape = PolygonShape()
        shape.setAsBox(config.botBox.width/2.0f, config.botBox.height/2.0f)
        fixtureDef.shape = shape
    }

    fun start() {
        logger.info { "Starting ArenaManager" }
        running.set(true)
        state = ArenaState.STARTED
        job = launch {
            updateWorld()
        }
    }

    fun register(outgoing: StreamObserver<FrameUpdate>): ServerRobot {
        logger.info { "Registering a new bot" }
        var robot: ServerRobot? = null

        robotLock.withLock {
            if(activeRobots.size == 0){
                state = ArenaState.SIMULATION_RUNNING
            }
            robot = ServerRobot(outgoing, body = createRobotBody())
            activeRobots.add(robot!!)
            logger.info { "Created robot with initial configuration: ${robot!!.getState()}" }
        }
        return robot!!
    }

    private fun createRobotBody() : Body{
        val robotCenter = findRobotSpot()
        val def = BodyDef()
        def.type = BodyType.DYNAMIC
        //bots are placed facing the center of the cartesian coordinates
        def.angle = Math.atan2(robotCenter.y - 0.0, robotCenter.x - 0.0).toFloat()
        def.position = robotCenter
        val body = world.createBody(def)
        body.createFixture(fixtureDef)

        return body

    }

    private fun findRobotSpot(): Vec2 {
        val xLow = config.botBox.width * 1.1f
        val xHi = config.screen.width - (config.botBox.width * 1.1f)
        val yLow = config.botBox.height * 1.1f
        val yHi = config.screen.height - (config.botBox.height * 1.1f)

        return Vec2(MathUtils.randomFloat(xLow, xHi), MathUtils.randomFloat(yLow, yHi))

    }


    fun watch(channel: Channel<ArenaView>) {
        logger.info { "Adding a new ArenaWatcher" }
        watchers.add(channel)
    }

    private suspend fun updateWorld() {

        currentTime = System.currentTimeMillis()

        while (isRunning()) {
            currentTime += ticks
            delta = currentTime - System.currentTimeMillis()
            if(delta <= 0) {
                delta = ticks.toLong()
            }
            robotLock.withLock {
                if(this.state == ArenaState.SIMULATION_RUNNING) {
                    filterDisconnected()
                    world.step(1.0f/30, 8, 3)
                    activeRobots.forEach { it.broadcast() }
                }

                if(watchers.isNotEmpty()) {
                    val view = ArenaView(this.id, this.state, System.currentTimeMillis(), activeRobots.map { it.getState() }, projectiles.map { it.getState() })
                    for (channel in watchers) {
                        channel.send(view)
                    }
                }
            }

            if (delta > 0) {
                delay(delta)
            }
        }
    }

    private fun filterDisconnected(){
        val iterator = activeRobots.iterator()
        while (iterator.hasNext()){
            val bot = iterator.next()
            if(!bot.isConnected()){
                println("Filtering out bot")
                world.destroyBody(bot.body)
                iterator.remove()
            }
        }
    }


    fun stop() {
        logger.info { "Shutting down arena Manager, open connections: { ${activeRobots.size + destroyedRobots.size} }" }
        running.set(false)
        job.cancel()

    }

    private fun isRunning(): Boolean {
        return running.get()
    }
}