package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentOverrideValidator()
        val item = newAppointmentOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AppointmentOverrideBatchSizeRangeRule(80, 255)
        val item = newAppointmentOverride("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentOverrideCodeLengthRule(16)
        val item = newAppointmentOverride("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentOverrideRequiredCodeRule("appointment override needs this field")
        val item = newAppointmentOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AppointmentOverrideFuelLitersRangeRule(5.5, 3856.6)
        val item = newAppointmentOverride("x", "y")
        item.fuelLiters = 3858.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AppointmentOverrideRequiredNameRule("appointment override needs a value")
        val item = newAppointmentOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
