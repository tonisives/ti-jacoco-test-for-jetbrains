import io.github.cdimascio.dotenv.dotenv
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.request.path
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.Koin
import org.slf4j.event.Level

fun main() {
    val port = dotenv().get("WEBSERVER_INTERNAL_PORT").toInt()

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        main()
    }.start(wait = true)
}

fun Application.main() {
    install(Koin) {
        modules(appModules)
    }

    configureLogging()
    configureSerialization()
    configureAuth()
    configureRouting()
}

fun Application.configureAuth() {
    // no auth
}

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
            }
        )
    }
}
