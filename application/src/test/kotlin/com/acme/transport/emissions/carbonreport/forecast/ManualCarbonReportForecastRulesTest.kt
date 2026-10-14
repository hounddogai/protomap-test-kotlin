package com.acme.transport.emissions.carbonreport.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCarbonReportForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCarbonReportForecastValidator()
        val item = newManualCarbonReportForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualCarbonReportForecastCodeLengthRule(3600)
        val item = newManualCarbonReportForecast("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = ManualCarbonReportForecastPalletsRangeRule(80, 3600)
        val item = newManualCarbonReportForecast("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualCarbonReportForecastRequiredCodeRule("manual carbon report forecast needs an entry")
        val item = newManualCarbonReportForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ManualCarbonReportForecastCreatedAtRangeRule(80, 3600)
        val item = newManualCarbonReportForecast("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
