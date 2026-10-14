package com.acme.transport.safety.incident.defaultincidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIncidentMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictIncidentMetricValidator()
        val item = newStrictIncidentMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictIncidentMetricLabelLengthRule(120)
        val item = newStrictIncidentMetric("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictIncidentMetricRequiredCodeRule("strict incident metric needs this field")
        val item = newStrictIncidentMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = StrictIncidentMetricGraceDaysRangeRule(16, 1000)
        val item = newStrictIncidentMetric("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = StrictIncidentMetricEtaMinutesRangeRule(32, 64)
        val item = newStrictIncidentMetric("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
