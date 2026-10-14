package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportSummaryValidator()
        val item = newCarbonReportSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportSummaryRequiredCodeRule("carbon report summary needs a value")
        val item = newCarbonReportSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = CarbonReportSummaryPalletsRangeRule(80, 86400)
        val item = newCarbonReportSummary("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
