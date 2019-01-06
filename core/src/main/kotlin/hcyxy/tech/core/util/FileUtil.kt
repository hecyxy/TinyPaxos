package hcyxy.tech.core.util

import com.google.gson.Gson
import hcyxy.tech.core.constants.PaxosConfig
import java.io.File

object FileUtil {
    fun file2Json(path: String): PaxosConfig {
        var content = ""
        val file = File(path)
        file.readLines().forEach { line ->
            content += line
        }
        val json = Gson()
        return json.fromJson(content, PaxosConfig::class.java)
    }
}