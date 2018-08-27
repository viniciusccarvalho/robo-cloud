package io.igx.cloud.robo.services

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.Action
import io.igx.cloud.robo.ActionType
import io.igx.cloud.robo.FrameUpdate
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureNanoTime

/**
 * @author vinicius
 *
 */


class GameServiceTests {

    @Test
    fun testArenaEngineUpdate()  {
        runBlocking {
            val gameService = GameService();
            gameService.start()
            val latch = CountDownLatch(150)
            val bot = MovingBot(latch)

            bot.actions.push(Action.newBuilder()
                    .setActionType(ActionType.ROTATE)
                    .setValue(1.0)
                    .setTimestamp(System.currentTimeMillis())
                    .build())
            bot.connect(gameService.connect(bot.so))
            bot.await()
            println(bot.lastFrame)
        }

    }


}

class MovingBot(val latch: CountDownLatch) {

    lateinit var lastFrame: FrameUpdate
    lateinit var outgoing: StreamObserver<Action>
    val connected = AtomicBoolean(false)
    var actions = LinkedList<Action>()

    val so: StreamObserver<FrameUpdate> = object : StreamObserver<FrameUpdate> {
        override fun onNext(value: FrameUpdate?) {
            onFrame(value!!)
        }

        override fun onError(t: Throwable?) {
        }

        override fun onCompleted() {
        }

    }

    fun connect(client: StreamObserver<Action>) {
        outgoing = client
        connected.set(true)
    }

    fun onFrame(frame: FrameUpdate){
        lastFrame = frame
        latch.countDown()
        if(connected.get()){
            val action = actions.poll()
            action?.let { outgoing.onNext(it) }
        }
    }

    fun await(){
        latch.await()
    }


}