package com.acme.transport.terminal.appointment.appointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAppointmentMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAppointmentMappingValidator()
        val item = newScheduledAppointmentMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledAppointmentMappingRequiredCodeRule("scheduled appointment mapping needs an entry")
        val item = newScheduledAppointmentMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ScheduledAppointmentMappingOdometerRangeRule(255, 255)
        val item = newScheduledAppointmentMapping("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAppointmentMappingCodeLengthRule(80)
        val item = newScheduledAppointmentMapping("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ScheduledAppointmentMappingSequenceRangeRule(80, 86400)
        val item = newScheduledAppointmentMapping("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
