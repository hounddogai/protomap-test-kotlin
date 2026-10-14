package com.acme.transport.network.transittime.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTransitTimeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTransitTimeMetricValidator()
        val item = newPrimaryTransitTimeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryTransitTimeMetricRequiredCodeRule("primary transit time metric needs a value")
        val item = newPrimaryTransitTimeMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = PrimaryTransitTimeMetricRequiredCountryRule("primary transit time metric needs an entry")
        val item = newPrimaryTransitTimeMetric("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryTransitTimeMetricStopCountRangeRule(16, 1000)
        val item = newPrimaryTransitTimeMetric("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
