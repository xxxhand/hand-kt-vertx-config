package hand.kt.verticles

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

/**
 * Created by hand on 2018/3/1.
 */
class ApiServerVerticle: AbstractVerticle() {
    override fun start(startFuture: Future<Void>?) {

        val serverOption = HttpServerOptions()
        serverOption.port = config().getInteger("port")

        val apiServer = vertx.createHttpServer(serverOption)
        val mainRouter = Router.router(vertx)
        mainRouter.route().handler(BodyHandler.create())
        mainRouter.route("/io").handler { it.response().end("This is io") }

        apiServer
                .requestHandler { mainRouter.accept(it) }
                .listen { evt ->
                    if (evt.failed()) {
                        evt.cause().printStackTrace()
                        startFuture?.fail(evt.cause())
                        return@listen
                    }
                    println("Server up on ${apiServer.actualPort()}")
                    startFuture?.complete()
                }
    }
}