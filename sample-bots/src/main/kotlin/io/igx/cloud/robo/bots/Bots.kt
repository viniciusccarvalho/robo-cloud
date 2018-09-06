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

class SpinnerBot(service: GameServiceGrpc.GameServiceStub, val move: Boolean = false) : BaseBot(service){

    var initialized = false
    var lastFrameUpdate: FrameUpdate

    init {
        lastFrameUpdate = FrameUpdate.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setRobotState(Robot.newBuilder()
                        .setHealth(100)
                        .build())
                .build()
    }

    override fun onFrame(frameUpdate: FrameUpdate) {
        if(!initialized){
            println("Initializing Spinner Bot")
            send(Action.newBuilder()
                    .setTimestamp(System.currentTimeMillis())
                    .setActionType(ActionType.ROTATE)
                    .setValue(1.0f)
                    .build())
            if(move){
                send(Action.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setActionType(ActionType.THROTTLE)
                        .setValue(1.0f)
                        .build())
            }
            initialized = true
        }
        if(frameUpdate.eventType == EventType.ENEMY_DETECTED){
            send(Action.newBuilder()
                    .setTimestamp(System.currentTimeMillis())
                    .setActionType(ActionType.FIRE)
                    .setValue(1.0f)
                    .build())
        }
        lastFrameUpdate = frameUpdate
    }

    fun disconnect(){
        this.client.onCompleted()
    }

    fun isAlive() : Boolean = lastFrameUpdate.robotState.health > 0

}