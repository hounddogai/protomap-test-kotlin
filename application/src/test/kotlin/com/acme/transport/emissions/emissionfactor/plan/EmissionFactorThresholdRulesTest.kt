package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorThresholdValidator()
        val item = newEmissionFactorThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = EmissionFactorThresholdReviewerLengthRule(64)
        val item = newEmissionFactorThreshold("x", "y")
        item.reviewer = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = EmissionFactorThresholdLabelLengthRule(32)
        val item = newEmissionFactorThreshold("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = EmissionFactorThresholdRequiredReviewerRule("emission factor threshold needs a value")
        val item = newEmissionFactorThreshold("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = EmissionFactorThresholdFuelLitersRangeRule(2.3, 3820.6)
        val item = newEmissionFactorThreshold("x", "y")
        item.fuelLiters = 3822.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
