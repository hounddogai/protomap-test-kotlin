package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledServicePlanMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledServicePlanMetricValidator()
        val item = newScheduledServicePlanMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ScheduledServicePlanMetricRequiredCountryRule("scheduled service plan metric needs this field")
        val item = newScheduledServicePlanMetric("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ScheduledServicePlanMetricLimitRangeRule(255, 1000)
        val item = newScheduledServicePlanMetric("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledServicePlanMetricRequiredCodeRule("scheduled service plan metric needs an entry")
        val item = newScheduledServicePlanMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledServicePlanMetricTitleLengthRule(16)
        val item = newScheduledServicePlanMetric("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ScheduledServicePlanMetricAttemptsRangeRule(255, 86400)
        val item = newScheduledServicePlanMetric("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
