package io.igx.cloud.robo.services

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.proto.FrameUpdate
import io.igx.cloud.robo.simulation.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.collision.shapes.ChainShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.contacts.Contact
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

    private var liveBots = mutableMapOf<String, ServerRobot>()
    private var destroyedBots = mutableListOf<ServerRobot>()
    private var projectiles = mutableMapOf<String, ServerProjectile>()
    private var robotLock = ReentrantLock()
    private val worldLock = ReentrantLock()
    private var watchers = mutableListOf<Channel<ArenaView>>()
    private val FPS = 30
    private val ticks = 1000 / FPS
    private val bulletSpeed = 200.0f
    var running = AtomicBoolean(false)
    var delta = 0L
    private var currentTime = System.currentTimeMillis()
    private lateinit var job: Job
    lateinit var state: ArenaState
    private val logger = KotlinLogging.logger {}
    private val world: World = World(Vec2())
    private val robotFixture: FixtureDef = FixtureDef()
    private val bulletFixture: FixtureDef = FixtureDef()
    private val callback = object : ArenaCallback {
        override fun onFireEvent(robot: ServerRobot) {
            createProjectile(robot)
        }
    }

    init {
        val robotShape = PolygonShape()
        robotShape.setAsBox(config.botBox.width/2.0f, config.botBox.height/2.0f)
        robotFixture.shape = robotShape

        val bulletShape = PolygonShape()
        bulletShape.setAsBox(6.0f, 6.0f )
        bulletFixture.shape = bulletShape
        bulletFixture.setSensor(true)

        val worldBounds = ChainShape()
        val vertices = arrayOf(Vec2(0.0f,0.0f),
                                           Vec2(0.0f, config.screen.height.toFloat()),
                                           Vec2(config.screen.width.toFloat(), config.screen.height.toFloat()),
                                           Vec2(config.screen.width.toFloat(), 0.0f))
        worldBounds.createLoop(vertices, vertices.size)
        val bodyDef = BodyDef()
        bodyDef.userData = BodyData(FixtureType.WALL, emptyMap())
        val bBody = world.createBody(bodyDef)
        bBody.createFixture(worldBounds, 0.0f)

        world.setContactListener(object : ContactListener {
            override fun endContact(contact: Contact?) {

            }

            override fun beginContact(contact: Contact?) {
                contact?.let { ct ->
                    solveContact(ct.fixtureA.body, ct.fixtureB.body)
                }
            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
            }

        })
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
            if(liveBots.size == 0){
                state = ArenaState.SIMULATION_RUNNING
            }
            val id = UUID.randomUUID().toString()
            val body = createRobotBody(id)
            robot = ServerRobot(id, outgoing, body = body, callback = callback)
            liveBots[id] = robot!!
            logger.info { "Created robot with initial configuration: ${robot!!.getState()}" }
        }
        return robot!!
    }

    private fun createRobotBody(id: String) : Body{
        val robotCenter = findRobotSpot()
        val def = BodyDef()
        def.userData = BodyData(FixtureType.ROBOT, mapOf("id" to id))
        def.type = BodyType.DYNAMIC
        //bots are placed facing the center of the cartesian coordinates
        def.angle = Math.atan2(robotCenter.y - 0.0, robotCenter.x - 0.0).toFloat()
        def.position = robotCenter
        worldLock.withLock {
            val body = world.createBody(def)
            body.createFixture(robotFixture)
            return body
        }
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

                    liveBots.values.forEach { robot ->
                        robot.updateCoordinates(delta)
                        liveBots.values.forEach{ target ->
                            if(robot.id != target.id){
                                robot.scanTarget(target)
                            }
                        }
                    }
                    worldLock.withLock {
                        world.step(1.0f/30, 8, 3)
                    }
                    liveBots.values.forEach { it.broadcast() }

                    if(watchers.isNotEmpty()) {
                        val view = ArenaView(this.id, this.state, System.currentTimeMillis(), liveBots.values.map { fromProto( it.getState() )}, projectiles.values.map { fromProto( it.getState() ) })
                        val iterator = watchers.iterator()
                        while (iterator.hasNext()){
                            val channel = iterator.next()
                            try{
                                channel.send(view)
                            } catch (e: Exception){
                                iterator.remove()
                            }
                        }
                    }
                }
            }
            if(liveBots.isEmpty()){
                reset()
            }
            if (delta > 0) {
                delay(delta)
            }
        }
    }

    /**
     * Solves contact between two entities. Depending on the type different Events/Actions can be yield
     *
     * The rules are the following
     * between a wall and a bullet : the bullet is destroyed and source robot gets reloaded
     * between two bullets, they both get destroyed and source bots get reloaded
     * between a bullet and a robot: bullet gets destroyed source bot gets reloaded, targeted bot gets a hit
     */
    private fun solveContact(source: Body, target: Body) {
        val sourceData = source.userData as BodyData
        val targetData = target.userData as BodyData

        when(sourceData.type){
            FixtureType.WALL -> {
                if(targetData.type == FixtureType.BULLET){
                    logger.info { "Collision between wall and bullet" }
                    destroyBullet(target)
                }
            }
            FixtureType.ROBOT -> {
                if(targetData.type == FixtureType.BULLET){
                    if(sourceData.context["id"] != targetData.context["robotid"]) {
                        val sourceBot = liveBots[sourceData.context["id"]]
                        val targetBot = liveBots[targetData.context["robotId"]]
                        targetBot?.let { sourceBot?.hitBy(it) }
                        destroyBullet(target)
                    }
                }
            }
            FixtureType.BULLET -> {
                if(targetData.type == FixtureType.BULLET){
                    destroyBullet(target)
                    destroyBullet(source)
                }else if(targetData.type == FixtureType.ROBOT){
                    if(targetData.context["id"] != sourceData.context["robotId"]) {
                        val targetBot = liveBots[targetData.context["id"]]
                        val sourceBot = liveBots[sourceData.context["robotId"]]
                        targetBot?.let { sourceBot?.hitBy(it) }
                        destroyBullet(source)
                    }
                }
            }
        }
    }

    private fun destroyBullet(bullet: Body){
        val data = bullet.userData as BodyData
        liveBots[data.context["robotId"]]?.reload()
        robotLock.withLock {
            projectiles.remove(data.context["id"])
        }
        worldLock.withLock { world.destroyBody(bullet) }

    }

    private fun reset() {
        this.state = ArenaState.STARTED
        robotLock.withLock {
            worldLock.withLock {
                projectiles.values.forEach{ world.destroyBody(it.body) }
            }
            projectiles.clear()
        }
    }

    /**
     * Creates a new projectile shot from the robot and place it on the World
     */
    private fun createProjectile(robot: ServerRobot) {
        val id = UUID.randomUUID().toString()
        val bodyDef = BodyDef()
        bodyDef.angle = robot.body.angle
        bodyDef.type = BodyType.KINEMATIC
        bodyDef.bullet = true
        bodyDef.position = robot.body.position.moveTo(robot.body.angle, (config.botBox.width / 2.0f) + config.bulletBox.width * 1.2f )
        bodyDef.userData = BodyData(FixtureType.BULLET, mapOf("id" to id, "robotId" to robot.id))
        val y = 1000 *  MathUtils.sin(bodyDef.angle) * bulletSpeed
        val x = 1000 * MathUtils.cos(bodyDef.angle) * bulletSpeed
        bodyDef.linearVelocity = Vec2(x, y)
        worldLock.withLock {
            val bullet = world.createBody(bodyDef)
            bullet.createFixture(bulletFixture)
            projectiles[id] = ServerProjectile(bullet, id)
        }


    }

    /**
     * Removes any Robot that is on disconnect state from the list
     */
    private fun filterDisconnected(){
        val iterator = liveBots.values.iterator()
        while (iterator.hasNext()){
            val bot = iterator.next()
            if(!bot.isConnected()){

                worldLock.withLock { world.destroyBody(bot.body) }
                iterator.remove()
            }
        }
    }

    private fun fromProto(robotProto: io.igx.cloud.robo.proto.Robot) : Robot {
        return Robot(robotProto.id, robotProto.name, Box(robotProto.box.bearing, Coordinates(robotProto.box.coordinates.x.toInt(), robotProto.box.coordinates.y.toInt())))
    }

    private fun fromProto(projectileProto: io.igx.cloud.robo.proto.Projectile) : Projectile {
        return Projectile(projectileProto.robotId, projectileProto.robotId, Box(projectileProto.box.bearing, Coordinates(projectileProto.box.coordinates.x.toInt(), projectileProto.box.coordinates.y.toInt())))
    }


    fun stop() {
        logger.info { "Shutting down arena Manager, open connections: { ${liveBots.size + destroyedBots.size} }" }
        running.set(false)
        job.cancel()

    }

    private fun isRunning(): Boolean {
        return running.get()
    }
}