package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset
import kotlin.test.assertEquals

object Day15 {
    fun evaluate(resource: String): Int {
        return Resources.toString(Resources.getResource(resource), Charset.defaultCharset())
            .split(",")
            .map {
                var chksum = 0
                for (chr in it) {
                    chksum = (chksum + chr.code) * 17 % 256
                }
                chksum
            }
            .sum()
    }
}

fun main() {
    //Day15.evaluate("2023/15/sample").also { assertEquals(1320, it) }
    Day15.evaluate("2023/15/puzzle").also { assertEquals(516469, it) }
}