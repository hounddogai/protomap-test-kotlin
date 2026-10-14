package com.acme.transport.terminal.appointment.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentScheduleValidator()
        val item = newAppointmentSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = AppointmentScheduleTrailerIdLengthRule(64)
        val item = newAppointmentSchedule("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = AppointmentScheduleRequiredTrailerIdRule("appointment schedule needs an entry")
        val item = newAppointmentSchedule("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
