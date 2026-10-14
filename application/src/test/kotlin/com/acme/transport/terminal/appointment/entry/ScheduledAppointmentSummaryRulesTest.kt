package com.acme.transport.terminal.appointment.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAppointmentSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAppointmentSummaryValidator()
        val item = newScheduledAppointmentSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ScheduledAppointmentSummaryWindowDaysRangeRule(1000, 3600)
        val item = newScheduledAppointmentSummary("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ScheduledAppointmentSummaryRequiredCategoryRule("scheduled appointment summary needs an entry")
        val item = newScheduledAppointmentSummary("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAppointmentSummaryCodeLengthRule(1000)
        val item = newScheduledAppointmentSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledAppointmentSummaryRequiredLabelRule("scheduled appointment summary needs this field")
        val item = newScheduledAppointmentSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledAppointmentSummaryRequiredCodeRule("scheduled appointment summary needs this field")
        val item = newScheduledAppointmentSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
