package io.igx.cloud.robo.services

import io.igx.cloud.robo.ServerRobot
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author vinicius
 * The ArenaService is responsible to maintain the state of a match, update the frames,
 * detect collisions.
 */
class ArenaService {

    var activeRobots = mutableListOf<ServerRobot>()
    var destroyedRobots = mutableListOf<ServerRobot>()
    val FPS = 25
    val ticks = 1000 / FPS
    var running = AtomicBoolean(false)
    var sleep = 0L
    var currentTime = System.currentTimeMillis()

    suspend fun update() {
        while(isRunning()){
            currentTime += ticks
            sleep = currentTime - System.currentTimeMillis()
            if(sleep > 0){
                delay(sleep)
            }
        }
    }

    private fun isRunning() : Boolean {
        return running.get()
    }
}