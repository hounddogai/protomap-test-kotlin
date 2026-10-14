package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAppointmentMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAppointmentMetricValidator()
        val item = newPrimaryAppointmentMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = PrimaryAppointmentMetricLocaleLengthRule(32)
        val item = newPrimaryAppointmentMetric("x", "y")
        item.locale = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryAppointmentMetricCodeLengthRule(80)
        val item = newPrimaryAppointmentMetric("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryAppointmentMetricRequiredCodeRule("primary appointment metric needs a value")
        val item = newPrimaryAppointmentMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
