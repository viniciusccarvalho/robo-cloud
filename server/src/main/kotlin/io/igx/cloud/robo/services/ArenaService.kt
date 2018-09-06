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
import org.jbox2d.callbacks.RayCastCallback
import org.jbox2d.collision.Manifold
import org.jbox2d.collision.shapes.ChainShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.contacts.Contact
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author vinicius
 * The ArenaService is responsible to maintain the state of a match, update the frames,
 * detect collisions.
 */
class ArenaService(val id: String = UUID.randomUUID().toString()) {

    private val logger = KotlinLogging.logger {}
    private val world: World = World(Vec2())
    private val robotFixture: FixtureDef = FixtureDef()
    private val bulletFixture: FixtureDef = FixtureDef()
    private val helper = GameHelper

    private var liveBots = ConcurrentHashMap<String, ServerRobot>()
    private var destroyedBots = mutableMapOf<String, ServerRobot>()
    private var projectiles = ConcurrentHashMap<String, ServerProjectile>()
    //Holds the events that will make any changes to the World or to the bots and projectiles collections
    private var worldEvents = ConcurrentLinkedQueue<WorldEvent>()

    private val worldLock = ReentrantLock()
    private var watchers = mutableListOf<Channel<ArenaView>>()
    private val FPS = helper.engineConfig.fps
    private val ticks = 1000 / FPS
    var running = AtomicBoolean(false)
    var delta = 0L
    private var currentTime = System.currentTimeMillis()
    private lateinit var job: Job
    lateinit var state: ArenaState


    val range = MathUtils.sqrt((helper.worldWidth*helper.worldWidth) + (helper.worldHeight*helper.worldHeight))

    private val callback = object : ArenaCallback {
        override fun onFireEvent(robot: ServerRobot) {
            worldEvents.offer(WorldEvent(WorldEventType.CREATE_BULLET, mapOf("robotId" to robot.id)))
        }
    }

    init {
        val robotShape = PolygonShape()
        robotShape.setAsBox(helper.scaleToWorld(helper.engineConfig.botConfig.box.width) / 2.0f, helper.scaleToWorld(helper.engineConfig.botConfig.box.height) / 2.0f)
        robotFixture.shape = robotShape
        robotFixture.density = 1.0f
        robotFixture.friction = 0.3f

        val bulletShape = PolygonShape()
        bulletShape.setAsBox(helper.scaleToWorld(6.0f), helper.scaleToWorld(6.0f))
        bulletFixture.shape = bulletShape
        bulletFixture.setSensor(true)

        val worldBounds = ChainShape()
        val vertices = arrayOf( helper.screenToWorld(Vec2(0.0f, 0.0f)),
                helper.screenToWorld(Vec2(0.0f, helper.engineConfig.worldConfig.screen.height.toFloat())),
                helper.screenToWorld(Vec2(helper.engineConfig.worldConfig.screen.width.toFloat(), helper.engineConfig.worldConfig.screen.height.toFloat())),
                helper.screenToWorld(Vec2(helper.engineConfig.worldConfig.screen.width.toFloat(), 0.0f))
        )
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
        var robot: ServerRobot? = null

        worldLock.withLock {
            if (liveBots.size == 0) {
                state = ArenaState.SIMULATION_RUNNING
            }
            val id = UUID.randomUUID().toString()
            val body = createRobotBody(id)
            robot = ServerRobot(id, outgoing, body = body, callback = callback)
            liveBots[id] = robot!!
        }
        return robot!!
    }

    private fun createRobotBody(id: String): Body {
        val robotCenter = findRobotSpot()
        println("Creating bot on world position: $robotCenter and screen coordinates ${helper.worldToScreen(robotCenter)}" )
        val def = BodyDef()
        def.userData = BodyData(FixtureType.ROBOT, mapOf("id" to id))
        def.type = BodyType.DYNAMIC
        //bots are placed facing the center of the cartesian coordinates
        def.angle = Math.atan2(robotCenter.y - 0.0, robotCenter.x - 0.0).toFloat()
        def.position = robotCenter
        val body = world.createBody(def)
        body.createFixture(robotFixture)
        return body
    }

    private fun findRobotSpot(): Vec2 {
        val xLow = helper.engineConfig.botConfig.box.width * 1.1f
        val xHi = helper.engineConfig.worldConfig.screen.width - (helper.engineConfig.botConfig.box.width * 1.1f)
        val yLow = helper.engineConfig.botConfig.box.height * 1.1f
        val yHi = helper.engineConfig.worldConfig.screen.height - (helper.engineConfig.botConfig.box.height * 1.1f)
        val screenPosition = Vec2(MathUtils.randomFloat(xLow, xHi), MathUtils.randomFloat(yLow, yHi))
        return helper.screenToWorld(screenPosition)
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
            if (delta <= 0) {
                delta = ticks.toLong()
            }
            worldLock.withLock {
                if (this.state == ArenaState.SIMULATION_RUNNING) {
                    filterBots()
                    destroyBullets()
                    liveBots.values.forEach { robot ->
                        robot.updateCoordinates(delta)
                        world.raycast(RayCastCallback { fixture, _, _, fraction ->
                            val data = fixture.body.userData as BodyData
                            if(data.type == FixtureType.ROBOT){
                                robot.onRayCast(liveBots[data.context["id"]]!!)
                            }
                            fraction
                        }, robot.body.position, robot.body.position.moveTo(robot.body.angle, range))
                    }
                    handleWorldEvents()
                    world.step(1.0f / 30, 8, 3)
                    liveBots.values.forEach { it.broadcast() }
                }
                if (watchers.isNotEmpty()) {
                    val view = ArenaView(this.id, this.state, System.currentTimeMillis(), liveBots.values.map { fromProto(it.getState()) }, projectiles.values.map { fromProto(it.getState()) })
                    val iterator = watchers.iterator()
                    while (iterator.hasNext()) {
                        val channel = iterator.next()
                        try {
                            channel.send(view)
                        } catch (e: Exception) {
                            iterator.remove()
                        }
                    }
                }
            }
            if (liveBots.isEmpty()) {
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

        when (sourceData.type) {
            FixtureType.WALL -> {
                if (targetData.type == FixtureType.BULLET) {
                    worldEvents.offer(WorldEvent(WorldEventType.DESTROY_BULLET, mapOf("id" to targetData.context["id"]!!)))
                }
            }
            FixtureType.ROBOT -> {
                if (targetData.type == FixtureType.BULLET) {
                    if (sourceData.context["id"] != targetData.context["robotId"]) {
                        worldEvents.offer(WorldEvent(WorldEventType.DESTROY_BULLET, mapOf("id" to targetData.context["id"]!!)))
                        worldEvents.offer(WorldEvent(WorldEventType.ROBOT_HIT, mapOf("robotId" to targetData.context["robotId"]!!,
                                "targetRobotId" to sourceData.context["id"]!!)))
                    }
                }
            }
            FixtureType.BULLET -> {
                if (targetData.type == FixtureType.BULLET) {
                    worldEvents.offer(WorldEvent(WorldEventType.DESTROY_BULLET, mapOf("id" to targetData.context["id"]!!)))
                    worldEvents.offer(WorldEvent(WorldEventType.DESTROY_BULLET, mapOf("id" to sourceData.context["id"]!!)))
                } else if (targetData.type == FixtureType.ROBOT) {
                    if (targetData.context["id"] != sourceData.context["robotId"]) {
                        worldEvents.offer(WorldEvent(WorldEventType.DESTROY_BULLET, mapOf("id" to sourceData.context["id"]!!)))
                        worldEvents.offer(WorldEvent(WorldEventType.ROBOT_HIT, mapOf("robotId" to sourceData.context["robotId"]!!,
                                "targetRobotId" to targetData.context["id"]!!)))
                    }
                }
            }
        }
    }

    /**
     * Handles
     */
    private fun handleWorldEvents() {
        while (!worldEvents.isEmpty()) {
            val event = worldEvents.poll()
            when (event.type) {
                WorldEventType.DESTROY_BULLET -> {
                    val bulletId = event.context["id"]
                    bulletId?.let {
                        val bullet = projectiles[it]
                        bullet?.let {
                            world.destroyBody(it.body)
                            val bot = liveBots[it.getRobotId()]
                            bot?.let {
                                it.reload()
                            }
                            projectiles.remove(it.id)
                        }
                    }
                }
                WorldEventType.CREATE_BULLET -> {
                    val botId = event.context["robotId"]
                    botId?.let {
                        val bot = liveBots[it]
                        bot?.let {
                            createProjectile(it)
                        }
                    }
                }
                WorldEventType.ROBOT_HIT -> {
                    val sourceId = event.context["robotId"]
                    sourceId?.let {
                        val sourceBot = liveBots[it]
                        val targetBot = liveBots[event.context["targetRobotId"]]
                        sourceBot?.let {
                            sourceBot.hitEnemy(targetBot)
                            targetBot?.let {
                                targetBot.hitBy(sourceBot)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Clears any bullet that has gone out of the world bounds (static to dynamic collision not working)
     * We use screen coordinates to simplify detection as it does not have negative numbers
     */
    private fun destroyBullets() {
        val iterator = projectiles.values.iterator()
        while (iterator.hasNext()) {
            val bullet = iterator.next()
            val pos = helper.worldToScreen(bullet.body.position)
            if (pos.x <= 0 || pos.x >= helper.engineConfig.worldConfig.screen.width || pos.y <= 0 || pos.y >= helper.engineConfig.worldConfig.screen.height) {
                worldEvents.offer(WorldEvent(WorldEventType.DESTROY_BULLET, mapOf("id" to bullet.id)))
            }
        }
    }

      private fun reset() {
        this.state = ArenaState.STARTED
        worldLock.withLock {
            projectiles.values.forEach { world.destroyBody(it.body) }
            liveBots.values.forEach { world.destroyBody(it.body) }
            projectiles.clear()
            liveBots.clear()
            destroyedBots.clear()
        }
    }

    /**
     * Creates a new projectile shot from the robot and place it on the World
     */
    private fun createProjectile(robot: ServerRobot) {
        val id = UUID.randomUUID().toString()
        val bodyDef = BodyDef()
        lateinit var bullet: Body
        bodyDef.angle = robot.body.angle
        bodyDef.type = BodyType.KINEMATIC
        bodyDef.bullet = true
        bodyDef.position = robot.body.position.moveTo(robot.body.angle, (helper.scaleToWorld(helper.engineConfig.botConfig.box.width) / 2.0f) + helper.scaleToWorld(helper.engineConfig.botConfig.box.width))
        bodyDef.userData = BodyData(FixtureType.BULLET, mapOf("id" to id, "robotId" to robot.id))
        val y = MathUtils.sin(bodyDef.angle) * helper.engineConfig.bulletConfig.speed
        val x = MathUtils.cos(bodyDef.angle) * helper.engineConfig.bulletConfig.speed
        bodyDef.linearVelocity = Vec2(x, y)
        bullet = world.createBody(bodyDef)
        bullet.createFixture(bulletFixture)
        projectiles[id] = ServerProjectile(bullet, id)
    }

    /**
     * Removes any disconnected bot and moves dead bots to destroyed list
     */
    private fun filterBots() {
        val iterator = liveBots.values.iterator()
        while (iterator.hasNext()) {
            val bot = iterator.next()
            if (!bot.isConnected()) {
                world.destroyBody(bot.body)
                iterator.remove()
            }
            if (!bot.isAlive()) {
                world.destroyBody(bot.body)
                destroyedBots[bot.id] = bot
                iterator.remove()
            }
        }
    }

    private fun fromProto(robotProto: io.igx.cloud.robo.proto.Robot): Robot {
        val vectors = liveBots[robotProto.id]?.radar?.points?.map { helper.worldToScreen(it) }
        val points = vectors?.map { Coordinates(it.x.toInt(), it.y.toInt()) }
        val worldPos = Vec2(robotProto.box.coordinates.x, robotProto.box.coordinates.y)
        val screenPosition = helper.worldToScreen(worldPos)
        return Robot(robotProto.id, robotProto.name, Box(robotProto.box.bearing * -1, Coordinates(screenPosition.x.toInt(), screenPosition.y.toInt())), points!!, robotProto.health, robotProto.score)
    }

    private fun fromProto(projectileProto: io.igx.cloud.robo.proto.Projectile): Projectile {
        val worldPos = Vec2(projectileProto.box.coordinates.x, projectileProto.box.coordinates.y)
        val screenPosition = helper.worldToScreen(worldPos)
        return Projectile(projectileProto.robotId, projectileProto.robotId, Box(projectileProto.box.bearing * -1, Coordinates(screenPosition.x.toInt(), screenPosition.y.toInt())))
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