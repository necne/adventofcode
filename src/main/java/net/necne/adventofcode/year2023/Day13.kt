package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset
import kotlin.math.min
import kotlin.test.assertEquals

object Day13 {
    fun evaluate(resource: String): Int {
        val puzzles = Resources.readLines(Resources.getResource(resource), Charset.defaultCharset())
            .fold(mutableListOf(mutableListOf<String>())) { acc, str ->
                if (str.isBlank()) acc.add(mutableListOf())
                else acc.last().add(str)
                acc
            }
            .map { findReflection(it) }

        println("$puzzles")

        return puzzles.sum()
    }

    private fun findReflection(strs: List<String>): Int {
        val hashes = strs.map { it.hashCode() }
        val pre = mutableListOf<Int>()
        var last: Int? = null;

        for (i in hashes.indices) {
            val ths = hashes[i]
            if (ths == last && compareListStart(pre, hashes.subList(i, hashes.size))) {
                return i * 100
            }
            pre.add(0, ths)
            last = ths
        }
        return findReflection(flip(strs)) / 100
    }

    private fun flip(strs: List<String>): List<String> {
        val flips = mutableListOf<String>()
        for (i in 0 until strs[0].length) {
            val sb = StringBuilder()
            for (str in strs) sb.append(str[i])
            flips.add(sb.toString())
        }
        return flips
    }

    private fun <T> compareListStart(list1: List<T>, list2: List<T>): Boolean {
        for (i in 0 until min(list1.size, list2.size)) {
            if (list1[i] != list2[i]) return false
        }
        return true
    }

}

fun main() {
    Day13.evaluate("2023/13/sample").also { assertEquals(405, it) }
    Day13.evaluate("2023/13/puzzle").also { assertEquals(27742, it) }
}