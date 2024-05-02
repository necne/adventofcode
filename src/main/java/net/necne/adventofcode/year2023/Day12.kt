package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset
import kotlin.test.assertEquals

const val separator = "."
val replacements = listOf("#", separator)
val spring_separation = Regex("\\.+")

fun springs(str: String): List<Int> {
    return str.split(spring_separation).filter { it.isNotEmpty() }.map { it.length }
}

fun arrangements(str: String, target: List<Int>): Int {
    val pos = str.indexOf("?")
    if (pos == -1) {
        if (target == springs(str)) return 1
        return 0
    }

    return replacements.sumOf { arrangements(StringBuilder(str).replace(pos, pos + 1, it).toString(), target) }
}

fun arrangements(str: String): Int {
    val parts = str.split(' ')
    val arrangement = parts[0]
    val target = parts[1].split(',').map { it.toInt() }
    val matches = arrangements(arrangement, target)

    //println("matches $matches in $str")

    return matches
}

fun evaluate(resource: String): Int {
    val matches =
        Resources.readLines(Resources.getResource(resource), Charset.defaultCharset()).sumOf { arrangements(it) }
    println("resource $resource total matches $matches")
    return matches
}

fun main() {
    evaluate("2023/12/sample").also { assertEquals(21, it) }
    evaluate("2023/12/puzzle").also { assertEquals(7007, it) }
}

