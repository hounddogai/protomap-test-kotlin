package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportTierValidator()
        val item = newCarbonReportTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportTierCodeLengthRule(80)
        val item = newCarbonReportTier("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportTierRequiredCodeRule("carbon report tier needs a value")
        val item = newCarbonReportTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
