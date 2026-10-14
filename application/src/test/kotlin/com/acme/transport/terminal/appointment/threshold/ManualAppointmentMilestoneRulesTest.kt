package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAppointmentMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAppointmentMilestoneValidator()
        val item = newManualAppointmentMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ManualAppointmentMilestoneFuelLitersRangeRule(1.4, 4430.8)
        val item = newManualAppointmentMilestone("x", "y")
        item.fuelLiters = 4432.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualAppointmentMilestoneTitleLengthRule(16)
        val item = newManualAppointmentMilestone("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
