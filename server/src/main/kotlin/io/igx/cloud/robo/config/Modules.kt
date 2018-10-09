package io.igx.cloud.robo.config

import io.igx.cloud.robo.services.GameServer
import io.igx.cloud.robo.services.GameService
import org.koin.dsl.module.module

/**
 * @author vinicius
 *
 */

val services = module("services") {
    single(name = "gameService"){ GameService() }
    single(name = "gameServer") { GameServer(get(name = "gameService")) }
}
