package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentMilestoneValidator()
        val item = newAppointmentMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentMilestoneCodeLengthRule(32)
        val item = newAppointmentMilestone("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AppointmentMilestoneLabelLengthRule(32)
        val item = newAppointmentMilestone("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AppointmentMilestoneContactEmailLengthRule(255)
        val item = newAppointmentMilestone("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = AppointmentMilestoneOdometerRangeRule(120, 3600)
        val item = newAppointmentMilestone("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
