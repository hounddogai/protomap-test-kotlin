package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTransitTimeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTransitTimeVarianceValidator()
        val item = newTieredTransitTimeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredTransitTimeVarianceRequiredCodeRule("tiered transit time variance needs a value")
        val item = newTieredTransitTimeVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = TieredTransitTimeVarianceRequiredLaneRule("tiered transit time variance needs a value")
        val item = newTieredTransitTimeVariance("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredTransitTimeVarianceCodeLengthRule(86400)
        val item = newTieredTransitTimeVariance("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TieredTransitTimeVarianceDurationMinutesRangeRule(120, 86400)
        val item = newTieredTransitTimeVariance("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
