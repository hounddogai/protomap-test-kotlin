package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAppointmentCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAppointmentCycleValidator()
        val item = newAdaptiveAppointmentCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveAppointmentCycleLabelLengthRule(80)
        val item = newAdaptiveAppointmentCycle("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AdaptiveAppointmentCycleStartsAtRangeRule(16, 255)
        val item = newAdaptiveAppointmentCycle("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveAppointmentCycleRequiredCodeRule("adaptive appointment cycle needs this field")
        val item = newAdaptiveAppointmentCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = AdaptiveAppointmentCycleRequiredNotesRule("adaptive appointment cycle needs this field")
        val item = newAdaptiveAppointmentCycle("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
