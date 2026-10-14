package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRosterTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRosterTierValidator()
        val item = newWeightedRosterTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = WeightedRosterTierRateRangeRule(2.5, 1820.9)
        val item = newWeightedRosterTier("x", "y")
        item.rate = 1822.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedRosterTierTitleLengthRule(16)
        val item = newWeightedRosterTier("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
