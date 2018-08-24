package io.igx.cloud.robo.controllers

import io.ktor.application.Application
import io.ktor.http.cio.websocket.Frame
import io.ktor.routing.routing
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

/**
 * @author vinicius
 *
 */
class ArenaWatcher(override val kodein: Kodein) : KodeinAware {

    val app: Application by instance()
    val frameUpdater: FrameUpdater

    init {
        frameUpdater = FrameUpdater()
        frameUpdater.start()
        app.routing {
            webSocket("/arenas") {
                val channel = Channel<Long>()
                try {
                    frameUpdater.subscribe(channel)
                    for (time in channel) {
                        outgoing.send(Frame.Text("timestamp: $time"))
                    }
                } catch (e: Exception) {
                    println("Will close the channel, connection gone bad? ${outgoing.isClosedForSend}")
                    channel.close()
                }
            }
        }
    }

}

class FrameUpdater() {

    val FPS = 25
    val ticks = 1000 / FPS
    var running = AtomicBoolean(true)
    var sleep = 0L
    var currentTime = System.currentTimeMillis()
    var watchers = mutableListOf<Channel<Long>>()
    val lock = ReentrantLock()

    fun subscribe(channel: Channel<Long>) {
        try {
            lock.lock()
            watchers.add(channel)
        }finally {
            lock.unlock()
        }

    }

    fun start() {
        launch { updateWorld() }
    }

    private suspend fun updateWorld() {
        while (isRunning()) {
            currentTime += ticks
            sleep = currentTime - System.currentTimeMillis()
            try {
                lock.lock()
                val iterator = watchers.iterator()
                while (iterator.hasNext()) {
                    val channel = iterator.next()
                    if (channel.isClosedForReceive) {
                        println("Removing this channel")
                        iterator.remove()
                    }else {
                        channel.send(sleep)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                lock.unlock()
            }
            if (sleep > 0) {
                delay(sleep)
            }
        }
    }

    private fun isRunning(): Boolean {
        return running.get()
    }
}