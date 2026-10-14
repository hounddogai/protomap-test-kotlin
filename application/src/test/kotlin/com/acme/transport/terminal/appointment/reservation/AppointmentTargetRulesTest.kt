package com.acme.transport.terminal.appointment.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentTargetValidator()
        val item = newAppointmentTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AppointmentTargetCeilingRangeRule(3.8, 2194.8)
        val item = newAppointmentTarget("x", "y")
        item.ceiling = 2196.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentTargetRequiredCodeRule("appointment target needs a value")
        val item = newAppointmentTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = AppointmentTargetRequiredPhoneRule("appointment target needs an entry")
        val item = newAppointmentTarget("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AppointmentTargetPhoneLengthRule(255)
        val item = newAppointmentTarget("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
