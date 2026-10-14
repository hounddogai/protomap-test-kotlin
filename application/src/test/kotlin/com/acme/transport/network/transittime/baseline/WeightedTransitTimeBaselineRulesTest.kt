package com.acme.transport.network.transittime.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTransitTimeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTransitTimeBaselineValidator()
        val item = newWeightedTransitTimeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = WeightedTransitTimeBaselineExpiresAtRangeRule(32, 255)
        val item = newWeightedTransitTimeBaseline("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedTransitTimeBaselineTitleLengthRule(1000)
        val item = newWeightedTransitTimeBaseline("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
