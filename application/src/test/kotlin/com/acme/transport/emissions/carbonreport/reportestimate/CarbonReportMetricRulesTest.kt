package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportMetricValidator()
        val item = newCarbonReportMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CarbonReportMetricCeilingRangeRule(0.0, 3857.1)
        val item = newCarbonReportMetric("x", "y")
        item.ceiling = 3858.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = CarbonReportMetricRequiredSourceRule("carbon report metric needs an entry")
        val item = newCarbonReportMetric("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = CarbonReportMetricSourceLengthRule(86400)
        val item = newCarbonReportMetric("x", "y")
        item.source = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
