package io.igx.cloud.robo.config

import io.igx.cloud.robo.controllers.ArenaWatcher
import io.igx.cloud.robo.controllers.GameController
import io.igx.cloud.robo.services.GameServer
import io.igx.cloud.robo.services.GameService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

/**
 * @author vinicius
 *
 */

val services = Kodein.Module(name = "services") {
    bind("gameService") from singleton { GameService() }
    bind("gameServer") from eagerSingleton { GameServer(instance("gameService")) }
}

val controllers = Kodein.Module(name = "controllers") {
    bind("arenaWatcher") from eagerSingleton { ArenaWatcher(kodein) }
    bind("gameController") from eagerSingleton { GameController(kodein) }
}