package io.igx.cloud.robo

import io.igx.cloud.robo.config.services
import io.igx.cloud.robo.controllers.controllers
import io.ktor.application.install
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import org.koin.ktor.ext.installKoin

/**
 * @author vinicius
 *
 */

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        installKoin(listOf(services))
        install(WebSockets)
        install(Routing){
            static("static") {
                resources("static")
            }
            controllers()
        }

    }.start(wait = true)
}

