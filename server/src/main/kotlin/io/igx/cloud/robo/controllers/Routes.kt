package io.igx.cloud.robo.controllers

import com.google.gson.Gson
import io.igx.cloud.robo.services.GameServer
import io.igx.cloud.robo.services.GameService
import io.igx.cloud.robo.simulation.ArenaView
import io.ktor.http.cio.websocket.Frame
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.Channel
import org.koin.ktor.ext.inject
import org.koin.standalone.inject

/**
 * @author vinicius
 *
 */
fun Routing.controllers() {
    servers()
    arenas()
}

fun Routing.servers() {
    val gameServer: GameServer by inject("gameServer")
    route("server"){
        post("start") {
            gameServer.start()
        }
        post("stop"){
            gameServer.stop()
        }
    }
}

fun Routing.arenas() {
    val gameService: GameService by inject("gameService")
    val gson = Gson()
    route("arenas"){
        webSocket {
            val channel = Channel<ArenaView>()
            try {
                gameService.watch(channel, 1)
                for (view in channel) {
                    outgoing.send(Frame.Text(gson.toJson(view)))
                }
            } catch (e: Exception) {
                channel.close()
            }
        }
    }
}