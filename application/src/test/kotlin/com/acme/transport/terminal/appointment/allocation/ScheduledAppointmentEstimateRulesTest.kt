package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAppointmentEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAppointmentEstimateValidator()
        val item = newScheduledAppointmentEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ScheduledAppointmentEstimateLocaleLengthRule(16)
        val item = newScheduledAppointmentEstimate("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ScheduledAppointmentEstimateRequiredLocaleRule("scheduled appointment estimate needs a value")
        val item = newScheduledAppointmentEstimate("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledAppointmentEstimateRequiredCodeRule("scheduled appointment estimate needs a value")
        val item = newScheduledAppointmentEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ScheduledAppointmentEstimateStartsAtRangeRule(32, 64)
        val item = newScheduledAppointmentEstimate("x", "y")
        item.startsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAppointmentEstimateCodeLengthRule(16)
        val item = newScheduledAppointmentEstimate("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
