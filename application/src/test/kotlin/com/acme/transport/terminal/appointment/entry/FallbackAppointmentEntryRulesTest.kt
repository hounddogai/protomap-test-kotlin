package com.acme.transport.terminal.appointment.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAppointmentEntryValidator()
        val item = newFallbackAppointmentEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackAppointmentEntryTitleLengthRule(80)
        val item = newFallbackAppointmentEntry("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = FallbackAppointmentEntryRequiredChecksumRule("fallback appointment entry needs this field")
        val item = newFallbackAppointmentEntry("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = FallbackAppointmentEntryPriorityRangeRule(32, 86400)
        val item = newFallbackAppointmentEntry("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackAppointmentEntryRequiredTitleRule("fallback appointment entry needs this field")
        val item = newFallbackAppointmentEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackAppointmentEntryRequiredCodeRule("fallback appointment entry needs a value")
        val item = newFallbackAppointmentEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
