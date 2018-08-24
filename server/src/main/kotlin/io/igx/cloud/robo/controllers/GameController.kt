package io.igx.cloud.robo.controllers

import io.igx.cloud.robo.services.GameServer
import io.ktor.application.Application
import io.ktor.routing.post
import io.ktor.routing.routing
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

/**
 * @author vinicius
 *
 */
class GameController(override val kodein: Kodein) : KodeinAware {

    val app: Application by instance()
    val gameServer: GameServer by instance("gameServer")

    init {
        app.routing {
            post("/server/start"){
                gameServer.start()
            }
            post("/server/stop"){
                gameServer.stop()
            }
        }
    }

}