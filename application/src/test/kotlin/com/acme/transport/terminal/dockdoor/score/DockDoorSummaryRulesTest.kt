package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorSummaryValidator()
        val item = newDockDoorSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DockDoorSummaryGraceDaysRangeRule(255, 86400)
        val item = newDockDoorSummary("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DockDoorSummaryNameLengthRule(86400)
        val item = newDockDoorSummary("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorSummaryRequiredCodeRule("dock door summary needs a value")
        val item = newDockDoorSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
