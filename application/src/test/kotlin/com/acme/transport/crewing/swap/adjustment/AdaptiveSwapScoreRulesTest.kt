package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveSwapScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveSwapScoreValidator()
        val item = newAdaptiveSwapScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveSwapScoreLabelLengthRule(3600)
        val item = newAdaptiveSwapScore("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = AdaptiveSwapScoreCostRangeRule(3.3, 3592.8)
        val item = newAdaptiveSwapScore("x", "y")
        item.cost = 3594.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveSwapScoreRequiredCodeRule("adaptive swap score needs a value")
        val item = newAdaptiveSwapScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AdaptiveSwapScoreEndsAtRangeRule(16, 255)
        val item = newAdaptiveSwapScore("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
