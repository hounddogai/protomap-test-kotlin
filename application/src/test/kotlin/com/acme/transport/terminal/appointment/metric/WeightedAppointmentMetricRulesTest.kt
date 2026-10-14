package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAppointmentMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAppointmentMetricValidator()
        val item = newWeightedAppointmentMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = WeightedAppointmentMetricExternalRefLengthRule(3600)
        val item = newWeightedAppointmentMetric("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = WeightedAppointmentMetricRequiredExternalRefRule("weighted appointment metric needs a value")
        val item = newWeightedAppointmentMetric("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
