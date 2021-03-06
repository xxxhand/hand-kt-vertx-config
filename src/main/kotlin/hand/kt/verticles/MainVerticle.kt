package hand.kt.verticles

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject

import hand.kt.enums.ConfigEvents
import io.vertx.core.DeploymentOptions
import kotlinx.coroutines.experimental.*
import java.util.concurrent.CompletableFuture

/**
 * Created by hand on 2018/3/1.
 */

class MainVerticle: AbstractVerticle() {
    private val verticles: MutableList<String> = mutableListOf()
    private val oldVerticles = mutableListOf<String>()

    override fun start(startFuture: Future<Void>?) {
        vertx.eventBus().consumer<JsonObject>(ConfigEvents.CONFIG_CHANG.value, { msg ->
            oldVerticles.clear()
            verticles.map { oldVerticles.add(it) }
            verticles.clear()
            doUnDeploy()
            doDeploy(msg.body())
        })
        startFuture!!.complete()
    }
    private fun doDeploy(config: JsonObject) {
        println("Do deploy")
        val deploymentOptions = DeploymentOptions()
        deploymentOptions.config = config

        vertx.deployVerticle(ApiServerVerticle::class.java.name, deploymentOptions, { res ->
            if (res.succeeded()) {
                println("Add verticle id: ${res.result()}")
                verticles.add(res.result())
            }
        })

    }
    private fun doUnDeploy() {
        println("Do un deploy")
        oldVerticles.map { vertx.undeploy(it, { res ->
            if (res.failed()) {
                res.cause().printStackTrace()
                return@undeploy
            }
            println("Success un deploy $it")
        }) }
    }

}