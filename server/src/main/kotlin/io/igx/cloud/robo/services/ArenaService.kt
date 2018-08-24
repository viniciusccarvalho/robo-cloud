package io.igx.cloud.robo.services

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.FrameUpdate
import io.igx.cloud.robo.ServerRobot
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author vinicius
 * The ArenaService is responsible to maintain the state of a match, update the frames,
 * detect collisions.
 */
class ArenaService {

    var activeRobots = mutableListOf<ServerRobot>()
    var destroyedRobots = mutableListOf<ServerRobot>()
    var robotLock = ReentrantLock()
    var watchers = mutableListOf<Channel<Long>>()
    val FPS = 25
    val ticks = 1000 / FPS
    var running = AtomicBoolean(false)
    var delta = 0L
    var currentTime = System.currentTimeMillis()
    lateinit var job: Job
    private val logger = KotlinLogging.logger {}


    fun start() {
        logger.info { "Starting ArenaManager" }
        running.set(true)
        job = launch {
            updateWorld()
        }
    }

    fun register(outgoing: StreamObserver<FrameUpdate>) : ServerRobot {
        val robot = ServerRobot(outgoing)
        logger.info { "Registering a new bot" }
        robotLock.withLock {
            activeRobots.add(robot)
        }
        return robot
    }

    fun watch(channel: Channel<Long>) = watchers.add(channel)

    private suspend fun updateWorld() {
        while(isRunning()){
            currentTime += ticks
            delta = currentTime - System.currentTimeMillis()

            robotLock.withLock {
                for(robot in activeRobots){
                    robot.updateCoordinates(delta)
                }
                activeRobots.forEach { it.broadcast() }
            }

            for(channel in watchers){
                channel.send(System.currentTimeMillis())
            }

            if(delta > 0){
                delay(delta)
            }
        }
    }

    /**
     * @TODO Implement Spatial Hashmap for regional collision check
     */
    private fun collisionCheck() {

    }

    fun stop() {
        logger.info { "Shutting down arena Manager, open connections: { ${activeRobots.size + destroyedRobots.size} }" }
        running.set(false)
        job.cancel()

    }

    private fun isRunning() : Boolean {
        return running.get()
    }
}