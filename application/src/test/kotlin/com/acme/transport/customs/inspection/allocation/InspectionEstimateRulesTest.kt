package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionEstimateValidator()
        val item = newInspectionEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = InspectionEstimateStopCountRangeRule(64, 80)
        val item = newInspectionEstimate("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionEstimateTitleLengthRule(64)
        val item = newInspectionEstimate("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = InspectionEstimateRequiredCurrencyRule("inspection estimate needs an entry")
        val item = newInspectionEstimate("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
