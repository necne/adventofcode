package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset
import kotlin.test.assertEquals

object Day15 {
    enum class Command(val code: String) {
        PUT("=") {
            override fun execute(lenses: MutableList<LensPower>, lensCommand: LensCommand) {
                val matches = lenses.filter { lensCommand.label == it.label }
                if (matches.isEmpty()) lenses.add(LensPower(lensCommand.label, lensCommand.power!!))
                else matches.forEach { it.power = lensCommand.power!! }
            }
        },
        REM("-") {
            override fun execute(lenses: MutableList<LensPower>, lensCommand: LensCommand) {
                lenses.removeIf { lensCommand.label == it.label }
            }
        };

        abstract fun execute(lenses: MutableList<LensPower>, lensCommand: LensCommand)

        companion object {
            fun fromCode(code: String): Command? {
                return entries.find { code == it.code }
            }
        }
    }

    fun sumCommandHashes(resource: String): Int {
        return Resources.toString(Resources.getResource(resource), Charset.defaultCharset())
            .split(",").sumOf { hash(it) }
    }

    fun sumFocusPower(resource: String): Int {
        val cmds = Resources.toString(Resources.getResource(resource), Charset.defaultCharset())
            .split(",")
            .map { toLensCommand(it) }

        val box_lenses = mutableMapOf<Int, MutableList<LensPower>>()
        for (cmd in cmds) {
            val box = hash(cmd.label)
            if (!box_lenses.containsKey(box)) box_lenses[box] = mutableListOf<LensPower>()
            cmd.cmd.execute(box_lenses[box]!!, cmd)

            println("cmd $cmd\n${toString(box_lenses)}");
        }

        return box_lenses
            .map {
                var focusPower = 0
                for (slot in it.value.indices) {
                    focusPower += (it.key + 1) * (slot + 1) * it.value[slot].power
                }
                focusPower
            }
            .sum()
    }

    private fun toString(box_lenses: Map<Int, List<LensPower>>): String {
        return box_lenses
            .entries
            .toList()
            .sortedBy { it.key }
            .joinToString(separator = "\n") { "  box ${it.key}: ${it.value.joinToString { "${it.label} ${it.power}" }}" }
    }

    private val commandLens = """([a-z]+)([=-])(\d?)""".toRegex()

    private fun toLensCommand(str: String): LensCommand {
        val (label, cmd, power) = commandLens.find(str)!!.destructured
        return LensCommand(label, Command.fromCode(cmd)!!, power.toIntOrNull())
    }

    data class LensCommand(val label: String, val cmd: Command, val power: Int?)

    data class LensPower(val label: String, var power: Int)

    private fun hash(str: String): Int {
        var chksum = 0
        for (chr in str) {
            chksum = (chksum + chr.code) * 17 % 256
        }
        return chksum
    }
}

fun main() {
    Day15.sumCommandHashes("2023/15/sample").also { assertEquals(1320, it) }
    Day15.sumCommandHashes("2023/15/puzzle").also { assertEquals(516469, it) }

    Day15.sumFocusPower("2023/15/sample").also { assertEquals(145, it) }
    Day15.sumFocusPower("2023/15/puzzle").also { assertEquals(221627, it) }
}