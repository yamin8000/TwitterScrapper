package io.github.yamin8000.twitterscrapper.helpers

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import io.github.yamin8000.twitterscrapper.helpers.ConsoleHelper.table
import java.util.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.KVisibility

@OptIn(com.github.ajalt.mordant.terminal.ExperimentalTerminalApi::class)
object ConsoleHelper {

    private val tableSubjectStyle = TextColors.blue + TextStyles.bold

    private val affirmatives = listOf(
        "y",
        "yes",
        "true",
        "1",
        "yep",
        "yeah",
        "yup",
        "yuh",
        "بله",
        "آره",
        "باشه",
        "نعم",
        "да",
        "давай",
        "давайте",
        "si",
        "oui",
        "ja",
        "ok",
        "okay"
    )

    val t = Terminal()
    val resultStyle = TextColors.green
    val infoStyle = TextColors.brightMagenta + TextStyles.bold
    val errorStyle = TextColors.red + TextStyles.bold
    val askStyle = TextColors.cyan + TextStyles.bold
    val warningStyle = TextColors.yellow + TextStyles.bold
    val menuStyle = TextColors.blue + TextStyles.bold

    private const val integerInputFailure = "Please enter a number only, try again!"

    /**
     * Prompts the user for an [Integer] input with the given optional [message],
     * the number must be between [range] if included otherwise any [Integer] is acceptable.
     * Eventually return the input as [Int]
     */
    fun readInteger(message: String? = null, range: IntRange? = null): Int {
        if (message != null) t.println(askStyle(message))
        return try {
            val input = readCleanLine()
            if (input.isNotBlank() && input.all { it.isDigit() }) {
                val number = input.toInt()
                if (number.isInRange(range)) number
                else readIntegerAfterFailure("Input is out of range.", message, range)
            } else readIntegerAfterFailure(integerInputFailure, message, range)
        } catch (exception: NumberFormatException) {
            readIntegerAfterFailure(integerInputFailure, message, range)
        }
    }

    /**
     * Extension function for checking if [Int] is in the given [range]
     */
    private fun Int.isInRange(range: IntRange?) = range == null || this in range

    /**
     * If the input is not valid (refer to [readInteger]), prompt the user again for an [Integer] input
     */
    private fun readIntegerAfterFailure(error: String, message: String?, range: IntRange?): Int {
        t.println(errorStyle(error))
        return readInteger(message, range)
    }

    /**
     * Prompts the user for a [Boolean] input with the given optional [message],
     * and eventually return the input as [Boolean]
     */
    fun readBoolean(message: String? = null): Boolean {
        return try {
            if (message != null) t.println(askStyle(message))
            readCleanLine().lowercase(Locale.getDefault()) in affirmatives
        } catch (exception: Exception) {
            false
        }
    }

    /**
     * Prompts the user to press enter to continue
     */
    fun pressEnterToContinue(message: String = "continue...") {
        t.println((TextColors.yellow on TextColors.black)("Press enter to $message"))
        readCleanLine()
    }

    /**
     * Prompts the user for entering multiple [String] values,
     * and eventually return a [List] of [String]
     */
    fun readMultipleStrings(field: String): List<String> {
        t.println(askStyle("Please enter ${infoStyle("$field/${field}s")}"))
        t.println(askStyle("If there are more than one ${infoStyle(field)} separate them using a comma (${infoStyle(",")})"))
        t.print(askStyle("Example: "))
        t.println("${randHsv()("Jackie")},${randHsv()("Elon")},${randHsv()("Taylor")},${randHsv()("Gary")}")
        val input = readCleanLine().split(",").map { it.trim() }
        return if (input.isValid()) {
            t.println(errorStyle("Please enter at least one $field."))
            readMultipleStrings(field)
        } else input
    }

    /**
     * Prompts the user for a single [String] value,
     * and eventually return the input as [String]
     */
    fun readSingleString(field: String): String {
        t.println(askStyle("Please enter ") + infoStyle(field))
        return readCleanLine().ifBlank {
            t.println(errorStyle("Input cannot be empty, try again!"))
            this.readSingleString(field)
        }
    }

    fun <T> T.table() = buildString {
        this@table!!::class.memberProperties.forEach {
            if (it.visibility == KVisibility.PUBLIC) {
                appendLine(it.name)
                appendLine("${it.getter.call(this@table)}")
            }
        }
    }

    fun <T> T.printTable() {
        this.table().split('\n').forEachIndexed { index, line ->
            if (index % 2 == 0)
                t.println(tableSubjectStyle(line))
            else t.println(infoStyle(line))
        }
    }

    /**
     * Reads a line from input and [String.trim] it,
     * if the input is null then returns an empty [String]
     */
    private fun readCleanLine(): String = readlnOrNull() ?: "".trim()

    private fun List<String>.isValid() = !(this.isEmpty() || this.all { it.isNotEmpty() })

    private fun randHsv() = t.colors.hsv((0..360).random(), 1, 1)
}