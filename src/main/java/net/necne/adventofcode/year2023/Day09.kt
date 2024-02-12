package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset

// https://adventofcode.com/2023/day/9

private fun parseHistory(resource: String): List<String> =
    Resources.readLines(Resources.getResource(resource), Charset.defaultCharset())

private fun parseMeasure(history: String) = history.split(" ").map { it.toInt() }

private fun predict(measures: List<Int>): Pair<Int, Int> {
    var prev = 0
    var next = 0

    println("$measures")

    var differences = measures.toMutableList()
    var odd = true
    while (differences.filter { it != 0 }.any()) {
        if (odd) prev += differences.first()
        else prev -= differences.first()
        odd = !odd
        next += differences.last()

        val nextDifferences = mutableListOf<Int>()
        for (i in 1..<differences.size) {
            nextDifferences.add(differences[i] - differences[i - 1])
        }
        println("  $nextDifferences")

        differences = nextDifferences
    }

    return Pair(prev, next)
}

private fun predict(resource: String) {
    val predictions =
        parseHistory(resource)
            .map { parseMeasure(it) }
            .map { predict(it) }

    print(
        """
> $resource
  prev ${predictions.sumOf { it.first }}
  next ${predictions.sumOf { it.second }}
"""
    )
}

fun main() {
    predict("2023/09/sample") // 2,114
    predict("2023/09/puzzle") // 864,1647269739
}
