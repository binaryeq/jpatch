package core.projectKb

import org.slf4j.Logger
import java.lang.Exception
import java.nio.file.Paths
import kotlin.io.path.*
import com.beust.klaxon.*
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.nio.file.Path
import kotlin.reflect.KClass


@Serializable
data class Statement(
    @SerialName("vulnerability_id")
    val vulnerabilityID: String,

    val notes: List<Note>? = null,
    val fixes: List<Fix>? = null,
    val artifacts: List<Artifact>? = null
)

@Serializable
data class Artifact(
    val id: String,
    val reason: String,
    val affected: Boolean
)

@Serializable
data class Fix(
    val id: String,
    val commits: List<Commit>
)

@Serializable
data class Commit(
    val id: String,
    val repository: String
)

@Serializable
data class Note(
    val text: String? = null,
    val links: List<String>? = null
)

private data class KbDataItem(
    val statement: Statement,
    val cveDir: Path,
)

data class SourceChange(
    val cveId: String,
    val commitHash: String,
    val beforeDir: Path,
    val afterDir: Path,
)

interface KbDataProvider {
    fun getStatements(): List<Statement>
    fun getSourceChanges(): List<SourceChange>
}

class SimpleKbDataProvider(private val logger: Logger, private val kbConfig: KbConfig) : KbDataProvider {
    private val data: List<KbDataItem>

    init {
        data = loadKbData()
    }

    private fun loadKbData(): List<KbDataItem> {
        logger.info("start")

        val statementsDir = Path(kbConfig.projectKbRepoPath, "statements")
        if (!statementsDir.exists()) {
            throw Exception("statements directory not found")
        }

        val yamlConf = YamlConfiguration(strictMode = false)
        val yamlParser = Yaml(configuration = yamlConf)

        val data = statementsDir.listDirectoryEntries()
            .filter { it.isDirectory() }
            .map { cveDir ->
                val statementData =
                    yamlParser.decodeFromString<Statement>(Paths.get(cveDir.toString(), "statement.yaml").readText())

                KbDataItem(
                    cveDir = cveDir,
                    statement = statementData
                )
            }.toList()

        logger.info("end")

        return data
    }

    override fun getStatements(): List<Statement> =
        data.map { it.statement }.toList()


    override fun getSourceChanges(): List<SourceChange> =
        data.map { kbIt ->
            val cveId = kbIt.statement.vulnerabilityID
            if (kbIt.statement.fixes == null)
                return@map null

            val commitHashes = kbIt.statement.fixes.flatMap { fix ->
                fix.commits.map { it.id }
            }

            commitHashes.map { h ->
                SourceChange(
                    cveId = cveId,
                    commitHash = h,
                    beforeDir = Paths.get(kbIt.cveDir.toString(), h.split(":").first(), "before"),
                    afterDir = Paths.get(kbIt.cveDir.toString(), h.split(":").first(), "after"),
                )
            }
        }.filterNotNull()
        .flatten()
}