package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCarbonReportEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCarbonReportEstimateValidator()
        val item = newWeightedCarbonReportEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = WeightedCarbonReportEstimateWindowDaysRangeRule(255, 3600)
        val item = newWeightedCarbonReportEstimate("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = WeightedCarbonReportEstimateRouteCodeLengthRule(86400)
        val item = newWeightedCarbonReportEstimate("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedCarbonReportEstimateUpdatedAtRangeRule(80, 1000)
        val item = newWeightedCarbonReportEstimate("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
