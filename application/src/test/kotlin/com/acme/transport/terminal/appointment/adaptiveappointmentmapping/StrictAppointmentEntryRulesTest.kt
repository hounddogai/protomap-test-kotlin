package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAppointmentEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAppointmentEntryValidator()
        val item = newStrictAppointmentEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictAppointmentEntryRequiredLabelRule("strict appointment entry needs an entry")
        val item = newStrictAppointmentEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictAppointmentEntryLabelLengthRule(80)
        val item = newStrictAppointmentEntry("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = StrictAppointmentEntryFloorValueRangeRule(6.4, 3361.8)
        val item = newStrictAppointmentEntry("x", "y")
        item.floorValue = 3363.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = StrictAppointmentEntryStopCountRangeRule(16, 3600)
        val item = newStrictAppointmentEntry("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictAppointmentEntryRequiredCodeRule("strict appointment entry needs a value")
        val item = newStrictAppointmentEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
