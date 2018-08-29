package io.igx.cloud.robo.services

import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import java.net.InetSocketAddress

/**
 * @author vinicius
 *
 */
class GameServer(val gameService: GameService) {

    private val logger = KotlinLogging.logger {}
    val server: Server

    init {
        val serverBuilder = NettyServerBuilder.forAddress(InetSocketAddress("localhost", 5000))
        server = serverBuilder.addService(gameService).build()
    }

    fun start() {
        logger.info { "Starting Game Server" }
        gameService.start()
        launch {
            server.start().awaitTermination()
        }
    }

    fun stop() {
        logger.info { "Stopping Game Server" }
        server.shutdownNow().awaitTermination()
    }

}