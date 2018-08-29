package io.igx.cloud.robo.controllers

import com.google.gson.Gson
import io.igx.cloud.robo.services.GameService
import io.igx.cloud.robo.simulation.ArenaView
import io.ktor.application.Application
import io.ktor.http.cio.websocket.Frame
import io.ktor.routing.routing
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.Channel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

/**
 * @author vinicius
 *
 */
class ArenaWatcher(override val kodein: Kodein) : KodeinAware {

    val app: Application by instance()
    val gameService: GameService by instance("gameService")
    val gson = Gson()
    init {
        app.routing {
            webSocket("/arenas") {
                val channel = Channel<ArenaView>()
                try {
                    gameService.watch(channel, 1)
                    for (view in channel) {
                        outgoing.send(Frame.Text(gson.toJson(view)))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Will close the channel, connection gone bad? ${outgoing.isClosedForSend}")
                    channel.close()
                }
            }
        }
    }

}
