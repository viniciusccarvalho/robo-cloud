package io.igx.cloud.robo.services

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.*

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
    }

    fun stop() {
        arenaService.stop()
    }

}