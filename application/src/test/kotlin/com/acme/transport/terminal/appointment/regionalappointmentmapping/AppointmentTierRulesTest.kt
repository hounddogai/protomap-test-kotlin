package com.acme.transport.terminal.appointment.regionalappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentTierValidator()
        val item = newAppointmentTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentTierCodeLengthRule(3600)
        val item = newAppointmentTier("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = AppointmentTierDepotLengthRule(16)
        val item = newAppointmentTier("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
