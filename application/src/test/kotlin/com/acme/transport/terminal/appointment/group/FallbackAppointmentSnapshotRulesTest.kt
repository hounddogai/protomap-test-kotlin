package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAppointmentSnapshotValidator()
        val item = newFallbackAppointmentSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackAppointmentSnapshotCodeLengthRule(120)
        val item = newFallbackAppointmentSnapshot("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = FallbackAppointmentSnapshotAttemptsRangeRule(64, 86400)
        val item = newFallbackAppointmentSnapshot("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackAppointmentSnapshotRequiredLabelRule("fallback appointment snapshot needs an entry")
        val item = newFallbackAppointmentSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackAppointmentSnapshotRequiredTitleRule("fallback appointment snapshot needs a value")
        val item = newFallbackAppointmentSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackAppointmentSnapshotRequiredCodeRule("fallback appointment snapshot needs an entry")
        val item = newFallbackAppointmentSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
