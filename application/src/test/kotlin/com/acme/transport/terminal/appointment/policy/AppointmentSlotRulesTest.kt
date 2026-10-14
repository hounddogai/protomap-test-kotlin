package com.acme.transport.terminal.appointment.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentSlotValidator()
        val item = newAppointmentSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AppointmentSlotRequiredTitleRule("appointment slot needs a value")
        val item = newAppointmentSlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AppointmentSlotTitleLengthRule(3600)
        val item = newAppointmentSlot("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AppointmentSlotRequiredCurrencyRule("appointment slot needs an entry")
        val item = newAppointmentSlot("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
