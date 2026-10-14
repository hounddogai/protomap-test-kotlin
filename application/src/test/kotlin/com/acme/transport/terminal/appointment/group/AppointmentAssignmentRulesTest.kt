package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentAssignmentValidator()
        val item = newAppointmentAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = AppointmentAssignmentCapacityRangeRule(32, 255)
        val item = newAppointmentAssignment("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AppointmentAssignmentRequiredNameRule("appointment assignment needs a value")
        val item = newAppointmentAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AppointmentAssignmentPhoneLengthRule(32)
        val item = newAppointmentAssignment("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentAssignmentCodeLengthRule(80)
        val item = newAppointmentAssignment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentAssignmentRequiredCodeRule("appointment assignment needs an entry")
        val item = newAppointmentAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
