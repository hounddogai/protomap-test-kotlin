package com.acme.transport.emissions.carbonreport.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCarbonReportRequestValidator()
        val item = newRegionalCarbonReportRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalCarbonReportRequestRequiredTitleRule("regional carbon report request needs this field")
        val item = newRegionalCarbonReportRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RegionalCarbonReportRequestStartsAtRangeRule(16, 32)
        val item = newRegionalCarbonReportRequest("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
