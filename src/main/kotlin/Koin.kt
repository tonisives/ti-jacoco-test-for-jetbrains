
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.dsl.module
import org.slf4j.LoggerFactory

val appModules = module {
    single { LoggerFactory.getLogger("logger") }
    single { dotenv() }
    single { HttpClient(CIO) }
}
