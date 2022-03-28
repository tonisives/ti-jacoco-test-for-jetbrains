
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.pipeline.PipelineContext
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    routing {
        route("/v1") {
            v1()
        }
    }
}

fun Route.v1() {
    val client by inject<HttpClient>()
    val dotenv = get<Dotenv>()


    // the client.post will not include the post in Jacoco test report
    post("/vehicles/{vin}/command/{property}") {
        // forward telematics command to mqtt
        val vin = call.parameters["vin"]!!
        val property = call.parameters["property"]!!
        val stringBody: String = call.receive()

        val port = dotenv.get("MQTT_PORT").toInt()
        val baseUrl = dotenv.get("BASE_URL")

        val response: HttpResponse = try {
            client.post("$baseUrl:$port/v1/vehicles/$vin/command/$property") {
                body = stringBody
            }
        } catch (e: ClientRequestException) {
            e.response
        }

        val stringResponseBody: String = response.receive()
        call.respond(response.status, stringResponseBody)

//        call.respond(HttpStatusCode.OK, "")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.respondNotFound(item: String) {
    call.respond(HttpStatusCode.NotFound, "Not found: $item")
}
