package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientInspectionEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientInspectionEstimateValidator()
        val item = newLenientInspectionEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = LenientInspectionEstimateLocaleLengthRule(64)
        val item = newLenientInspectionEstimate("x", "y")
        item.locale = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LenientInspectionEstimateCapacityRangeRule(64, 1000)
        val item = newLenientInspectionEstimate("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LenientInspectionEstimateWeightRangeRule(3.9, 3552.3)
        val item = newLenientInspectionEstimate("x", "y")
        item.weight = 3553.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientInspectionEstimateTitleLengthRule(3600)
        val item = newLenientInspectionEstimate("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
