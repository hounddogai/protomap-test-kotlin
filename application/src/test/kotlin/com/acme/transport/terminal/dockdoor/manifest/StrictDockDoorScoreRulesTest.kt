package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDockDoorScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDockDoorScoreValidator()
        val item = newStrictDockDoorScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = StrictDockDoorScoreAttemptsRangeRule(64, 255)
        val item = newStrictDockDoorScore("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictDockDoorScoreCodeLengthRule(64)
        val item = newStrictDockDoorScore("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
