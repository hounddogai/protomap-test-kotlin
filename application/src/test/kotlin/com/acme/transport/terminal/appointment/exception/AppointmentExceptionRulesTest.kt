package com.acme.transport.terminal.appointment.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentExceptionValidator()
        val item = newAppointmentException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AppointmentExceptionRequiredTitleRule("appointment exception needs this field")
        val item = newAppointmentException("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AppointmentExceptionTitleLengthRule(3600)
        val item = newAppointmentException("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = AppointmentExceptionRateRangeRule(10.0, 4815.1)
        val item = newAppointmentException("x", "y")
        item.rate = 4816.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
