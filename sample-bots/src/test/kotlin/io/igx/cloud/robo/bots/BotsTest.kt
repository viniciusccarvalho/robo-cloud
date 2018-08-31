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
                val spinnerBot = SpinnerBot(service)
                spinnerBot.connect()
                bots.add(spinnerBot)
            }
            delay(30000)
            bots.forEach {
                println(it.lastFrameUpdate)
                it.disconnect() }
            channel.shutdown()
        }


    }



}