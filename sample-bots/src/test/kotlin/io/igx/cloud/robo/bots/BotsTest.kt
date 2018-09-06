package io.igx.cloud.robo.bots

import io.grpc.ManagedChannelBuilder
import io.igx.cloud.robo.proto.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test


/**
 * @author vinicius
 *
 */

class SpinnerTest {

    @Test
    fun launchBots() {
        runBlocking {
            val channel = ManagedChannelBuilder.forAddress("localhost", 5000).usePlaintext().build()
            val service = GameServiceGrpc.newStub(channel)
            var bots = mutableListOf<SpinnerBot>()
            for (i in 1..4){
                val spinnerBot = SpinnerBot(service, true)
                spinnerBot.connect()
                bots.add(spinnerBot)
            }

            do {
                val alive = bots.count { it.isAlive() }
                println("Alive bots count : $alive")
                delay(5000)
                bots.forEach { println("${it.lastFrameUpdate.robotState.name} health: ${it.lastFrameUpdate.robotState.health} score: ${it.lastFrameUpdate.robotState.score}") }
            }while (bots.count { it.isAlive() } > 1)
            bots.forEach {
                println(it.lastFrameUpdate)
                it.disconnect() }
            channel.shutdown()
        }


    }



}