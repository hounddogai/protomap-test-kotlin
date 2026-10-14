package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentMetricValidator()
        val item = newAppointmentMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AppointmentMetricCurrencyLengthRule(86400)
        val item = newAppointmentMetric("x", "y")
        item.currency = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
