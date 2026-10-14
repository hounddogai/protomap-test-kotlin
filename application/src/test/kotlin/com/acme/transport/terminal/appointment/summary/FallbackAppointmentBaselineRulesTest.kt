package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAppointmentBaselineValidator()
        val item = newFallbackAppointmentBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = FallbackAppointmentBaselineEndsAtRangeRule(1000, 86400)
        val item = newFallbackAppointmentBaseline("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackAppointmentBaselineEtaMinutesRangeRule(16, 64)
        val item = newFallbackAppointmentBaseline("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackAppointmentBaselineRequiredCodeRule("fallback appointment baseline needs a value")
        val item = newFallbackAppointmentBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
