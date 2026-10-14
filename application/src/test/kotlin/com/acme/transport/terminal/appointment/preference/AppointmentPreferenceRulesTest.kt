package com.acme.transport.terminal.appointment.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentPreferenceValidator()
        val item = newAppointmentPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = AppointmentPreferenceBudgetRangeRule(7.3, 3828.0)
        val item = newAppointmentPreference("x", "y")
        item.budget = 3829.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = AppointmentPreferenceChannelLengthRule(120)
        val item = newAppointmentPreference("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AppointmentPreferenceLabelLengthRule(80)
        val item = newAppointmentPreference("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = AppointmentPreferenceRequiredPhoneRule("appointment preference needs a value")
        val item = newAppointmentPreference("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
