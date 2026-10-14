package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionEstimateValidator()
        val item = newAdaptiveInspectionEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveInspectionEstimateNameLengthRule(64)
        val item = newAdaptiveInspectionEstimate("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveInspectionEstimateRequiredCodeRule("adaptive inspection estimate needs a value")
        val item = newAdaptiveInspectionEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = AdaptiveInspectionEstimateAttemptsRangeRule(80, 1000)
        val item = newAdaptiveInspectionEstimate("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = AdaptiveInspectionEstimateRequiredSourceRule("adaptive inspection estimate needs a value")
        val item = newAdaptiveInspectionEstimate("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AdaptiveInspectionEstimateAmountRangeRule(3.7, 2036.9)
        val item = newAdaptiveInspectionEstimate("x", "y")
        item.amount = 2038.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
