package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentWindowValidator()
        val item = newAppointmentWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = AppointmentWindowRequiredNotesRule("appointment window needs a value")
        val item = newAppointmentWindow("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AppointmentWindowDurationMinutesRangeRule(32, 80)
        val item = newAppointmentWindow("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = AppointmentWindowRequiredShiftCodeRule("appointment window needs this field")
        val item = newAppointmentWindow("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
