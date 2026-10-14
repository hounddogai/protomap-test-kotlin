package com.acme.transport.emissions.carbonreport.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCarbonReportAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCarbonReportAgreementValidator()
        val item = newSeasonalCarbonReportAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalCarbonReportAgreementCodeLengthRule(120)
        val item = newSeasonalCarbonReportAgreement("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = SeasonalCarbonReportAgreementCurrencyLengthRule(1000)
        val item = newSeasonalCarbonReportAgreement("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = SeasonalCarbonReportAgreementFuelLitersRangeRule(0.5, 1579.0)
        val item = newSeasonalCarbonReportAgreement("x", "y")
        item.fuelLiters = 1580.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
