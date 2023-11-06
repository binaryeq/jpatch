package config

import core.projectKb.KbConfig
import core.githubAdvisory.GhsaConfig
import com.google.gson.Gson
import io.github.cdimascio.dotenv.Dotenv
import java.io.File

data class Config(val kbConfig: KbConfig, val ghsaConfig: GhsaConfig)

fun loadConfig(): Config {
    val dotenv = Dotenv.load()
    val resourcesDir = dotenv.get("RESOURCES")
    val resource = File(resourcesDir, "config.json").readText()
    return Gson().fromJson(resource, Config::class.java)
}