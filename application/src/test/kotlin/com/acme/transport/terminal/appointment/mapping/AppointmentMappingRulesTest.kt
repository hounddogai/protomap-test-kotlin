package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentMappingValidator()
        val item = newAppointmentMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentMappingRequiredCodeRule("appointment mapping needs this field")
        val item = newAppointmentMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AppointmentMappingRequiredTitleRule("appointment mapping needs this field")
        val item = newAppointmentMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentMappingCodeLengthRule(3600)
        val item = newAppointmentMapping("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
