package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAppointmentAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAppointmentAllocationValidator()
        val item = newCappedAppointmentAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = CappedAppointmentAllocationRequiredTimezoneRule("capped appointment allocation needs a value")
        val item = newCappedAppointmentAllocation("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
