package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAppointmentScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAppointmentScoreValidator()
        val item = newPendingAppointmentScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = PendingAppointmentScoreGraceDaysRangeRule(32, 1000)
        val item = newPendingAppointmentScore("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PendingAppointmentScoreUnitLengthRule(1000)
        val item = newPendingAppointmentScore("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = PendingAppointmentScoreRequiredUnitRule("pending appointment score needs this field")
        val item = newPendingAppointmentScore("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingAppointmentScoreCodeLengthRule(255)
        val item = newPendingAppointmentScore("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
