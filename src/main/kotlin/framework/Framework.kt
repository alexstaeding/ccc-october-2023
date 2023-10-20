package framework

import java.io.File
import java.io.FileNotFoundException

object Framework {

    @JvmStatic
    @JvmOverloads
    fun readInputLines(level: Int, num: Int, prefix: String = "level"): List<String> =
        tryAccessFile("$prefix$level") { resolve("$prefix${level}_$num.in").readLines() }

    @JvmStatic
    @JvmOverloads
    fun readInput(level: Int, num: Int, prefix: String = "level"): String =
        tryAccessFile("$prefix$level") { resolve("$prefix${level}_$num.in").readText().trim() }

    @JvmStatic
    @JvmOverloads
    fun writeOutput(level: Int, num: Int, output: String, prefix: String = "level") =
        tryAccessFile("$prefix$level") { resolve("$prefix${level}_$num.out").writeText(output) }

    /**
     * Try to access via gradle-set run directory, if not found, it means we are running from IDE configuration
     * and the "run" folder must be added to the path manually
     */
    private fun <T> tryAccessFile(name: String, block: File.() -> T): T {
        return try {
            File(name).block()
        } catch (e: FileNotFoundException) {
            File("run").resolve(name).block()
        }
    }
}
