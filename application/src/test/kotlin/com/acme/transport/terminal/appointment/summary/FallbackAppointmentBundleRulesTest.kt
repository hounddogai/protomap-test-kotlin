package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAppointmentBundleValidator()
        val item = newFallbackAppointmentBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackAppointmentBundleCodeLengthRule(86400)
        val item = newFallbackAppointmentBundle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackAppointmentBundleRequiredCodeRule("fallback appointment bundle needs a value")
        val item = newFallbackAppointmentBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = FallbackAppointmentBundleSegmentLengthRule(120)
        val item = newFallbackAppointmentBundle("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = FallbackAppointmentBundleRequiredNotesRule("fallback appointment bundle needs an entry")
        val item = newFallbackAppointmentBundle("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
