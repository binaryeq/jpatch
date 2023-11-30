package core.githubAdvisory

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.*

data class GhsaDataItem(
    val ghsaJson: GhsaJson,
    val year: Int,
    val month: Int,
    val ghsaJsonPath: Path
)

@Serializable
data class GhsaJson(
    @SerialName("schema_version")
    val schemaVersion: String,

    val id: String,
    val modified: String,
    val published: String,
    val withdrawn: String? = null,
    val aliases: List<String>,
    val summary: String? = null,
    val details: String,
    val severity: List<Severity>,
    val affected: List<Affected>,
    val references: List<Reference>,

    @SerialName("database_specific")
    val databaseSpecific: DatabaseSpecific
)

@Serializable
data class Affected(
    @SerialName("package")
    val affectedPackage: Package,
    @SerialName("ecosystem_specific")
    val ecosystemSpecific: EcosystemSpecific? = null,
    val ranges: List<Range>? = null,
    val versions: List<String>? = null,
    @SerialName("database_specific")
    val databaseSpecific: AffectedDatabaseSpecific? = null
)

@Serializable
data class AffectedDatabaseSpecific(
    @SerialName("last_known_affected_version_range")
    val lastKnownAffectedVersionRange: String
)

@Serializable
data class Package(
    val ecosystem: String,
    val name: String,
)

@Serializable
data class EcosystemSpecific(
    @SerialName("affected_functions")
    val affectedFunctions: List<String>
)

@Serializable
data class Range(
    val type: String,
    val events: List<Event>
)

@Serializable
data class Event(
    val introduced: String? = null,
    val fixed: String? = null,
    @SerialName("last_affected")
    val lastAffected: String? = null,
)

@Serializable
data class DatabaseSpecific(
    @SerialName("cwe_ids")
    val cweIDS: List<String>,

    val severity: String,

    @SerialName("github_reviewed")
    val githubReviewed: Boolean,

    @SerialName("github_reviewed_at")
    val githubReviewedAt: String,

    @SerialName("nvd_published_at")
    val nvdPublishedAt: String? = null
)

@Serializable
data class Reference(
    val type: String,
    val url: String
)

@Serializable
data class Severity(
    val type: String,
    val score: String
)

interface GhsaDataProvider {
    fun getJsonData(): List<GhsaJson>
}

class MavenReviewedGhsaDataProvider(
    private val logger: Logger,
    private val ghsaConfig: GhsaConfig
) : GhsaDataProvider {
    private val data: List<GhsaDataItem>

    init {
        data = loadData()
    }

    private fun loadData(): List<GhsaDataItem> {
        val ghsaReviewedDir = Path(ghsaConfig.githubAdvisoryRepoPath, "advisories", "github-reviewed").also {
            if (!it.exists()) {
                throw Exception("github-reviewed directory not found")
            }
        }

        val data =
            ghsaReviewedDir.listDirectoryEntries()
                .filter { it.isDirectory() }
                .flatMap { yearDir ->
                    yearDir.listDirectoryEntries()
                        .filter { it.isDirectory() }
                        .flatMap { monthDir ->
                            monthDir.listDirectoryEntries()
                                .filter { it.isDirectory() }
                                .flatMap { ghsaDir ->
                                    ghsaDir.listDirectoryEntries()
                                        .filter { ghsaFile -> ghsaFile.isRegularFile() && ghsaFile.fileName.toString().replace(Regex("\\.json$"), "") == ghsaDir.fileName.toString() }
                                        .map { ghsaFile ->
                                            GhsaDataItem(
                                                ghsaJson = Json.decodeFromString(ghsaFile.readText()),
                                                ghsaJsonPath = ghsaFile,
                                                year = yearDir.fileName.toString().toInt(),
                                                month = monthDir.fileName.toString().toInt()
                                            )
                                        }
                                }
                        }
                }

        return data
    }

    override fun getJsonData(): List<GhsaJson> {
        return data.map { it.ghsaJson }.filter { it.affected.first().affectedPackage.ecosystem == "Maven" }.toList()
    }
}

