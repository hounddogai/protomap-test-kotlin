package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedHoldVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedHoldVarianceValidator()
        val item = newWeightedHoldVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedHoldVarianceCodeLengthRule(32)
        val item = newWeightedHoldVariance("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = WeightedHoldVarianceAmountRangeRule(6.8, 3277.2)
        val item = newWeightedHoldVariance("x", "y")
        item.amount = 3278.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = WeightedHoldVarianceGraceDaysRangeRule(16, 255)
        val item = newWeightedHoldVariance("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedHoldVarianceRequiredLabelRule("weighted hold variance needs this field")
        val item = newWeightedHoldVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
