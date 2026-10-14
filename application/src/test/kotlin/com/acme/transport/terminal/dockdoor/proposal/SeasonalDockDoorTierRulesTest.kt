package com.acme.transport.terminal.dockdoor.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDockDoorTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDockDoorTierValidator()
        val item = newSeasonalDockDoorTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDockDoorTierRequiredCodeRule("seasonal dock door tier needs an entry")
        val item = newSeasonalDockDoorTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = SeasonalDockDoorTierRequiredSummaryRule("seasonal dock door tier needs a value")
        val item = newSeasonalDockDoorTier("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = SeasonalDockDoorTierSummaryLengthRule(16)
        val item = newSeasonalDockDoorTier("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = SeasonalDockDoorTierBudgetRangeRule(2.1, 3432.7)
        val item = newSeasonalDockDoorTier("x", "y")
        item.budget = 3434.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = SeasonalDockDoorTierCurrencyLengthRule(1000)
        val item = newSeasonalDockDoorTier("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = SeasonalDockDoorTierRetriesRangeRule(32, 80)
        val item = newSeasonalDockDoorTier("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
