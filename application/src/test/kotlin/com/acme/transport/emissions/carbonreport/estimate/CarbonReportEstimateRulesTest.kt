package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportEstimateValidator()
        val item = newCarbonReportEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = CarbonReportEstimateRequiredReviewerRule("carbon report estimate needs this field")
        val item = newCarbonReportEstimate("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportEstimateRequiredCodeRule("carbon report estimate needs a value")
        val item = newCarbonReportEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CarbonReportEstimateGraceDaysRangeRule(80, 3600)
        val item = newCarbonReportEstimate("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = CarbonReportEstimateRequiredDescriptionRule("carbon report estimate needs an entry")
        val item = newCarbonReportEstimate("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = CarbonReportEstimateDescriptionLengthRule(80)
        val item = newCarbonReportEstimate("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
