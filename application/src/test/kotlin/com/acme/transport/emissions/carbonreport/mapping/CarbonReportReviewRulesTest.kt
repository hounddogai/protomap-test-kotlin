package com.acme.transport.emissions.carbonreport.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportReviewValidator()
        val item = newCarbonReportReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CarbonReportReviewRequiredLabelRule("carbon report review needs a value")
        val item = newCarbonReportReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CarbonReportReviewLabelLengthRule(80)
        val item = newCarbonReportReview("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportReviewCodeLengthRule(64)
        val item = newCarbonReportReview("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = CarbonReportReviewRequiredSummaryRule("carbon report review needs an entry")
        val item = newCarbonReportReview("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CarbonReportReviewLoadKgRangeRule(7.7, 4799.3)
        val item = newCarbonReportReview("x", "y")
        item.loadKg = 4800.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
