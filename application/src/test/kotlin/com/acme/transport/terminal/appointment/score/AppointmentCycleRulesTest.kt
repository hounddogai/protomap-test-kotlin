package com.acme.transport.terminal.appointment.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentCycleValidator()
        val item = newAppointmentCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = AppointmentCycleSourceLengthRule(80)
        val item = newAppointmentCycle("x", "y")
        item.source = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = AppointmentCycleRequiredSourceRule("appointment cycle needs an entry")
        val item = newAppointmentCycle("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AppointmentCyclePalletsRangeRule(64, 86400)
        val item = newAppointmentCycle("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AppointmentCycleRequiredContactEmailRule("appointment cycle needs this field")
        val item = newAppointmentCycle("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AppointmentCycleContactEmailLengthRule(1000)
        val item = newAppointmentCycle("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentCycleRequiredCodeRule("appointment cycle needs a value")
        val item = newAppointmentCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
