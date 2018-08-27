package io.igx.cloud.robo.services

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.Action
import io.igx.cloud.robo.ActionType
import io.igx.cloud.robo.EventType
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
            val latch = CountDownLatch(1)
            val bot = MovingBot(latch)
            val bot2 = MovingBot(latch)
            bot.actions.push(Action.newBuilder()
                    .setActionType(ActionType.ROTATE)
                    .setValue(1.0)
                    .setTimestamp(System.currentTimeMillis())
                    .build())
            bot2.actions.push(Action.newBuilder()
                    .setActionType(ActionType.ROTATE)
                    .setValue(1.0)
                    .setTimestamp(System.currentTimeMillis())
                    .build())
            bot.connect(gameService.connect(bot.so))
            bot2.connect(gameService.connect(bot2.so))
            latch.await()

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

        if(frame.eventType == EventType.ENEMY_DETECTED) {
            latch.countDown()
            println("FIRE!!!! $frame")
        }
        if(connected.get()){
            val action = actions.poll()
            action?.let { outgoing.onNext(it) }
        }
    }




}