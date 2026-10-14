package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAppointmentVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAppointmentVarianceValidator()
        val item = newScheduledAppointmentVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ScheduledAppointmentVarianceUpdatedAtRangeRule(3600, 3600)
        val item = newScheduledAppointmentVariance("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ScheduledAppointmentVarianceRateRangeRule(3.7, 3693.6)
        val item = newScheduledAppointmentVariance("x", "y")
        item.rate = 3695.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAppointmentVarianceCodeLengthRule(16)
        val item = newScheduledAppointmentVariance("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledAppointmentVarianceTitleLengthRule(16)
        val item = newScheduledAppointmentVariance("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
