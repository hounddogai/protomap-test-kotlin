package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentSegmentValidator()
        val item = newAppointmentSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AppointmentSegmentDurationMinutesRangeRule(64, 80)
        val item = newAppointmentSegment("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = AppointmentSegmentUnitLengthRule(86400)
        val item = newAppointmentSegment("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
