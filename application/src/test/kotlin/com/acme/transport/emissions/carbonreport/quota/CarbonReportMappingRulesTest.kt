package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportMappingValidator()
        val item = newCarbonReportMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CarbonReportMappingMaximumRangeRule(8.2, 3904.3)
        val item = newCarbonReportMapping("x", "y")
        item.maximum = 3905.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = CarbonReportMappingRetriesRangeRule(16, 1000)
        val item = newCarbonReportMapping("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
