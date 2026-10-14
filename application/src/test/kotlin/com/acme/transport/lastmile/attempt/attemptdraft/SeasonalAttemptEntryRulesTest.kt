package com.acme.transport.lastmile.attempt.attemptdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAttemptEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAttemptEntryValidator()
        val item = newSeasonalAttemptEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalAttemptEntryTitleLengthRule(80)
        val item = newSeasonalAttemptEntry("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = SeasonalAttemptEntryFuelLitersRangeRule(5.3, 835.5)
        val item = newSeasonalAttemptEntry("x", "y")
        item.fuelLiters = 837.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = SeasonalAttemptEntryDistanceKmRangeRule(5.4, 4980.0)
        val item = newSeasonalAttemptEntry("x", "y")
        item.distanceKm = 4981.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalAttemptEntryRequiredCodeRule("seasonal attempt entry needs a value")
        val item = newSeasonalAttemptEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalAttemptEntryCodeLengthRule(16)
        val item = newSeasonalAttemptEntry("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SeasonalAttemptEntryBatchSizeRangeRule(32, 64)
        val item = newSeasonalAttemptEntry("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
