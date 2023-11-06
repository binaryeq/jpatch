import config.Config
import config.loadConfig
import core.githubAdvisory.GhsaDataProvider
import core.githubAdvisory.MavenReviewedGhsaDataProvider
import core.projectKb.KbDataProvider
import core.projectKb.SimpleKbDataProvider
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun setupServices() {
    startKoin {
        modules(
            module {
                single { _ -> LoggerFactory.getLogger("global") } bind Logger::class
            },
            module {
                single { _ -> loadConfig() } bind Config::class
            },
            module { single { _ -> SimpleKbDataProvider(get(), get<Config>().kbConfig) } bind KbDataProvider::class },
            module {
                single { _ ->
                    MavenReviewedGhsaDataProvider(
                        get(),
                        get<Config>().ghsaConfig
                    )
                } bind GhsaDataProvider::class
            }
        )
    }
}

fun main() {
}