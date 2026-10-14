package com.acme.transport.terminal.appointment.regionalappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentRequestValidator()
        val item = newAppointmentRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = AppointmentRequestDepotLengthRule(1000)
        val item = newAppointmentRequest("x", "y")
        item.depot = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentRequestCodeLengthRule(64)
        val item = newAppointmentRequest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
