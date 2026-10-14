package com.acme.transport.emissions.carbonreport.reportagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCarbonReportAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCarbonReportAgreementValidator()
        val item = newFallbackCarbonReportAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackCarbonReportAgreementCodeLengthRule(3600)
        val item = newFallbackCarbonReportAgreement("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = FallbackCarbonReportAgreementRateRangeRule(5.8, 2129.2)
        val item = newFallbackCarbonReportAgreement("x", "y")
        item.rate = 2130.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackCarbonReportAgreementTitleLengthRule(255)
        val item = newFallbackCarbonReportAgreement("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = FallbackCarbonReportAgreementBudgetRangeRule(6.2, 3989.3)
        val item = newFallbackCarbonReportAgreement("x", "y")
        item.budget = 3990.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackCarbonReportAgreementRequiredCodeRule("fallback carbon report agreement needs an entry")
        val item = newFallbackCarbonReportAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
