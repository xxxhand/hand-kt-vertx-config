package hand.kt

import hand.kt.enums.ConfigEvents
import hand.kt.verticles.MainVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import java.io.File

/**
 * Created by hand on 2018/2/27.
 */

fun main(args: Array<String>) {
    println("Hello world")

    val configPath = "configs/appConfig.dev.json"

    val vx = Vertx.vertx()
    val configRetriever = initialConfig(vx, configPath)

    configRetriever!!.getConfig { vx.eventBus().publish(ConfigEvents.CONFIG_CHANG.value, it.result()) }
    configRetriever.listen { vx.eventBus().publish(ConfigEvents.CONFIG_CHANG.value, it.newConfiguration) }


    vx.eventBus().consumer<String>(ConfigEvents.CONFIG_QUERY.value, { it.reply(configRetriever.cachedConfig) })

    vx.deployVerticle(MainVerticle::class.java.name)

}

private fun initialConfig(vertx: Vertx, filePath: String): ConfigRetriever? {
    val configFile = File(filePath)
    if (!configFile.exists()) {
        println("$filePath does not exist")
        return null
    }
    val crOptions = ConfigRetrieverOptions()
            .addStore(ConfigStoreOptions()
                    .setType("file")
                    .setFormat("json")
                    .setConfig(JsonObject().put("path", filePath))
            )

    return ConfigRetriever.create(vertx, crOptions)
}