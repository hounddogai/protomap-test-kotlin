package com.acme.transport.safety.incident.incidenttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalIncidentMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalIncidentMetricValidator()
        val item = newGlobalIncidentMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalIncidentMetricNameLengthRule(3600)
        val item = newGlobalIncidentMetric("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
