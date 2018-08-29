package io.igx.cloud.robo.bots

import io.grpc.stub.StreamObserver
import io.igx.cloud.robo.proto.*

/**
 * @author vinicius
 *
 */

abstract class BaseBot(val service: GameServiceGrpc.GameServiceStub) {

    abstract fun onFrame(frameUpdate: FrameUpdate)
    lateinit var client: StreamObserver<Action>

    val so: StreamObserver<FrameUpdate> = object : StreamObserver<FrameUpdate> {
        override fun onNext(value: FrameUpdate?) {
            onFrame(value!!)
        }

        override fun onError(t: Throwable?) {
            error(t)
        }

        override fun onCompleted() {
            onDisconnect()
        }

    }

    fun onDisconnect() {

    }

    fun error(t: Throwable?) {
        t?.printStackTrace()
    }

    fun connect() {
        this.client = service.connect(so)
    }

    fun send(action: Action){
        client.onNext(action)
    }
}

class SpinnerBot(service: GameServiceGrpc.GameServiceStub) : BaseBot(service){

    var initialized = false
    var lastFrameUpdate: FrameUpdate

    init {
        lastFrameUpdate = FrameUpdate.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .build()
    }

    override fun onFrame(frameUpdate: FrameUpdate) {
        println(frameUpdate)
        if(!initialized){
            println("Initializing Spinner Bot")
            send(Action.newBuilder()
                    .setTimestamp(System.currentTimeMillis())
                    .setActionType(ActionType.ROTATE)
                    .setValue(1.0f)
                    .build())
            send(Action.newBuilder()
                    .setTimestamp(System.currentTimeMillis())
                    .setActionType(ActionType.THROTTLE)
                    .setValue(1.0f)
                    .build())
            initialized = true
        }
    }

    fun disconnect(){
        this.client.onCompleted()

    }

}