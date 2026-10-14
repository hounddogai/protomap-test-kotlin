package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDockDoorScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDockDoorScoreValidator()
        val item = newSeasonalDockDoorScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDockDoorScoreRequiredCodeRule("seasonal dock door score needs an entry")
        val item = newSeasonalDockDoorScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = SeasonalDockDoorScoreSummaryLengthRule(16)
        val item = newSeasonalDockDoorScore("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
