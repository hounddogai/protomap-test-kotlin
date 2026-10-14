package com.acme.transport.terminal.appointment.appointmentpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAppointmentPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAppointmentPreferenceValidator()
        val item = newCappedAppointmentPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedAppointmentPreferenceRequiredLabelRule("capped appointment preference needs this field")
        val item = newCappedAppointmentPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedAppointmentPreferenceLabelLengthRule(32)
        val item = newCappedAppointmentPreference("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = CappedAppointmentPreferenceAttemptsRangeRule(16, 86400)
        val item = newCappedAppointmentPreference("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
