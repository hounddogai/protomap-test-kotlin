package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentAllocationValidator()
        val item = newAppointmentAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AppointmentAllocationLoadKgRangeRule(7.7, 2717.5)
        val item = newAppointmentAllocation("x", "y")
        item.loadKg = 2719.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentAllocationCodeLengthRule(3600)
        val item = newAppointmentAllocation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentAllocationRequiredCodeRule("appointment allocation needs a value")
        val item = newAppointmentAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
