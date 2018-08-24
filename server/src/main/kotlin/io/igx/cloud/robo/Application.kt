package io.igx.cloud.robo

import io.igx.cloud.robo.config.controllers
import io.igx.cloud.robo.config.services
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

/**
 * @author vinicius
 *
 */

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        kodeinApplication{ application ->
            application.install(WebSockets)
            application.routing {
                static("static") {
                    resources("static")
                }
            }
            import(services)
            import(controllers)
        }
    }.start(wait = true)
}

fun Application.kodeinApplication(kodeinMapper : Kodein.MainBuilder.(Application) -> Unit = {}) {
    val app = this
    val kodein = Kodein {
        bind<Application>() with singleton { app }
        kodeinMapper(this, app)
    }
}