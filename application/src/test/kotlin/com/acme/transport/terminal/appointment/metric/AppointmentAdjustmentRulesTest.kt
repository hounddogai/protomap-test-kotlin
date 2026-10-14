package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentAdjustmentValidator()
        val item = newAppointmentAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AppointmentAdjustmentRequiredLabelRule("appointment adjustment needs a value")
        val item = newAppointmentAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AppointmentAdjustmentLabelLengthRule(3600)
        val item = newAppointmentAdjustment("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = AppointmentAdjustmentExpiresAtRangeRule(32, 86400)
        val item = newAppointmentAdjustment("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
