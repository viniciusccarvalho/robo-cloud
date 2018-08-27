package io.igx.cloud.robo.services

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.FrameUpdate
import io.igx.cloud.robo.simulation.WorldConfig
import io.igx.cloud.robo.simulation.ServerRobot
import io.igx.cloud.robo.simulation.normalizeAngle
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author vinicius
 * The ArenaService is responsible to maintain the state of a match, update the frames,
 * detect collisions.
 */
class ArenaService(val config: WorldConfig = WorldConfig()) {

    var activeRobots = mutableListOf<ServerRobot>()
    var destroyedRobots = mutableListOf<ServerRobot>()
    var robotLock = ReentrantLock()
    var watchers = mutableListOf<Channel<Long>>()
    val FPS = 25
    val ticks = 1000 / FPS
    var running = AtomicBoolean(false)
    var delta = 0L
    var currentTime = System.currentTimeMillis()
    val random = Random()
    lateinit var job: Job
    private val logger = KotlinLogging.logger {}


    fun start() {
        logger.info { "Starting ArenaManager" }
        running.set(true)
        job = launch {
            updateWorld()
        }
    }

    fun register(outgoing: StreamObserver<FrameUpdate>): ServerRobot {
        logger.info { "Registering a new bot" }
        var robot: ServerRobot? = null
        robotLock.withLock {
            val robotCenter = findRobotSpot()
            val angle = normalizeAngle(Math.toDegrees(Math.atan2(robotCenter.y - 0.0, robotCenter.x - 0.0)))
            robot = ServerRobot(outgoing, center = robotCenter, bearing = angle)
            activeRobots.add(robot!!)
            logger.info { "Created robot with initial configuration: ${robot!!.getState()}" }

        }
        return robot!!
    }

    private fun findRobotSpot(): Vector2D {
        val xLimit = config.screen.width / 2 - config.botBox.width / 2
        val yLimit = config.screen.height / 2 - config.botBox.height / 2
        return Vector2D((random.nextInt(xLimit * 2) - xLimit).toDouble(), (random.nextInt(yLimit * 2) - yLimit).toDouble())
    }


    fun watch(channel: Channel<Long>) = watchers.add(channel)

    private suspend fun updateWorld() {
        currentTime = System.currentTimeMillis()
        while (isRunning()) {
            currentTime += ticks
            delta = currentTime - System.currentTimeMillis()
            if(delta <= 0) {
                delta = ticks.toLong()
            }
            robotLock.withLock {
                for (robot in activeRobots) {
                    robot.updateCoordinates(delta)
                }
                detectTargets()
                activeRobots.forEach { it.broadcast() }
            }

            for (channel in watchers) {
                channel.send(System.currentTimeMillis())
            }

            if (delta > 0) {
                delay(delta)
            }
        }
    }

    /**
     * @TODO Implement Spatial Hashmap for regional collision check
     */
    private fun collisionCheck() {

    }

    private fun detectTargets() {
        for(robot in activeRobots){
            for(target in activeRobots){
                if(target.id != robot.id){
                    robot.scanTarget(target)
                }
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