package io.igx.cloud.robo.services

import io.grpc.Context
import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.proto.*
import io.igx.cloud.robo.simulation.ArenaView
import kotlinx.coroutines.experimental.channels.Channel
import java.math.BigDecimal

/**
 * @author vinicius
 *
 */
class GameService(val arenas: Int = 1) : GameServiceGrpc.GameServiceImplBase() {

    private val arenaService = ArenaService()

    override fun connect(responseObserver: StreamObserver<FrameUpdate>?): StreamObserver<Action> {
        return arenaService.register(responseObserver!!).subscribe()
    }

    fun start(){
        arenaService.start()

        val num = BigDecimal(0)
        num + BigDecimal(3.0)
    }

    fun stop() {
        arenaService.stop()
    }

    fun watch(channel: Channel<ArenaView>, id: Int) {
        arenaService.watch(channel)
    }

}