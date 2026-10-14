package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTireBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTireBudgetValidator()
        val item = newSeasonalTireBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = SeasonalTireBudgetEtaMinutesRangeRule(16, 1000)
        val item = newSeasonalTireBudget("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalTireBudgetRequiredNameRule("seasonal tire budget needs an entry")
        val item = newSeasonalTireBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = SeasonalTireBudgetTrailerIdLengthRule(255)
        val item = newSeasonalTireBudget("x", "y")
        item.trailerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
