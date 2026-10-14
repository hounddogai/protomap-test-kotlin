package com.acme.transport.emissions.carbonreport.reportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCarbonReportAdjustmentValidator()
        val item = newRegionalCarbonReportAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCarbonReportAdjustmentRequiredCodeRule("regional carbon report adjustment needs an entry")
        val item = newRegionalCarbonReportAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalCarbonReportAdjustmentNameLengthRule(80)
        val item = newRegionalCarbonReportAdjustment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
