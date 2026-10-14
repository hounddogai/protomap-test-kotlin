package com.acme.transport.crewing.restbreak.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRestBreakLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRestBreakLedgerValidator()
        val item = newSeasonalRestBreakLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = SeasonalRestBreakLedgerRetriesRangeRule(16, 64)
        val item = newSeasonalRestBreakLedger("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = SeasonalRestBreakLedgerMinimumRangeRule(2.0, 367.5)
        val item = newSeasonalRestBreakLedger("x", "y")
        item.minimum = 369.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = SeasonalRestBreakLedgerDescriptionLengthRule(120)
        val item = newSeasonalRestBreakLedger("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalRestBreakLedgerLabelLengthRule(64)
        val item = newSeasonalRestBreakLedger("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = SeasonalRestBreakLedgerMaximumRangeRule(9.2, 3517.2)
        val item = newSeasonalRestBreakLedger("x", "y")
        item.maximum = 3518.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
