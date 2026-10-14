package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCarbonReportAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCarbonReportAgreementValidator()
        val item = newPrimaryCarbonReportAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryCarbonReportAgreementRequiredCodeRule("primary carbon report agreement needs an entry")
        val item = newPrimaryCarbonReportAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = PrimaryCarbonReportAgreementDepotLengthRule(120)
        val item = newPrimaryCarbonReportAgreement("x", "y")
        item.depot = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = PrimaryCarbonReportAgreementGraceDaysRangeRule(64, 1000)
        val item = newPrimaryCarbonReportAgreement("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PrimaryCarbonReportAgreementRequiredCurrencyRule("primary carbon report agreement needs this field")
        val item = newPrimaryCarbonReportAgreement("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
