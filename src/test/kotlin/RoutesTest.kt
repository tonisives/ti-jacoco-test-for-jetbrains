import io.github.cdimascio.dotenv.Dotenv
import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.utils.io.ByteReadChannel
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.After
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

private fun Application.test() {
    // routing from real code
    configureRouting()
    configureSerialization()
}

class RoutesTest : KoinTest {

    @Test
    fun forwardsSendCommandToMqtt() {
        val dotenv = mockk<Dotenv>()
        every { dotenv.get("MQTT_PORT") } returns "10"
        every { dotenv.get("BASE_URL") } returns "localhost"

        val mockEngine = MockEngine { request ->
            val encodedPath = request.url.encodedPath
            println(request.url.encodedPath)
            if (encodedPath == "/10/v1/vehicles/vin1/command/door_lock") {
                respond(
                    content = ByteReadChannel("""content"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondBadRequest()
            }
        }
        val client = HttpClient(mockEngine)

        val module = module {
            single { dotenv }
            single { client }
        }

        startKoin {
            modules(module)
        }

        val command = buildJsonObject {
            put("command", "command")
        }

        withTestApplication(Application::test) {
            handleRequest(HttpMethod.Post, "v1/vehicles/vin1/command/door_lock") {
                setBody(command.toString())
            }.apply {
//                expect(response.status()) { HttpStatusCode.OK }
//                expect(response.content) { "content" }
            }

            handleRequest(HttpMethod.Post, "v1/vehicles/vin1/command/invalid") {
                setBody(command.toString())
            }.apply {
//                expect(response.status()) { HttpStatusCode.BadRequest }
            }
        }
    }

    @After
    fun after() {
        stopKoin()
    }

}
