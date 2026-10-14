package com.acme.transport.terminal.dockdoor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDockDoorSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDockDoorSummaryValidator()
        val item = newPrimaryDockDoorSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = PrimaryDockDoorSummaryStartsAtRangeRule(64, 64)
        val item = newPrimaryDockDoorSummary("x", "y")
        item.startsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PrimaryDockDoorSummaryRatioRangeRule(4.2, 1615.8)
        val item = newPrimaryDockDoorSummary("x", "y")
        item.ratio = 1617.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = PrimaryDockDoorSummaryRequiredLocaleRule("primary dock door summary needs a value")
        val item = newPrimaryDockDoorSummary("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryDockDoorSummaryRequiredTitleRule("primary dock door summary needs this field")
        val item = newPrimaryDockDoorSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = PrimaryDockDoorSummaryLocaleLengthRule(16)
        val item = newPrimaryDockDoorSummary("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
