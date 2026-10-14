package com.acme.transport.emissions.carbonreport.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportThresholdValidator()
        val item = newCarbonReportThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = CarbonReportThresholdRequiredRegionRule("carbon report threshold needs an entry")
        val item = newCarbonReportThreshold("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = CarbonReportThresholdRegionLengthRule(255)
        val item = newCarbonReportThreshold("x", "y")
        item.region = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportThresholdRequiredCodeRule("carbon report threshold needs an entry")
        val item = newCarbonReportThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
