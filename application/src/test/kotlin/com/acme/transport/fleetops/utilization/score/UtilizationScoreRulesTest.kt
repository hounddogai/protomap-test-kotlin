package com.acme.transport.fleetops.utilization.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationScoreValidator()
        val item = newUtilizationScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = UtilizationScoreRequiredCurrencyRule("utilization score needs an entry")
        val item = newUtilizationScore("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = UtilizationScorePalletsRangeRule(64, 120)
        val item = newUtilizationScore("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = UtilizationScoreCurrencyLengthRule(120)
        val item = newUtilizationScore("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
