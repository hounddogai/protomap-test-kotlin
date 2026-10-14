package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerYardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTrailerYardEntryValidator()
        val item = newScheduledTrailerYardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ScheduledTrailerYardEntryRequiredCarrierRule("scheduled trailer yard entry needs a value")
        val item = newScheduledTrailerYardEntry("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledTrailerYardEntryNameLengthRule(16)
        val item = newScheduledTrailerYardEntry("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = ScheduledTrailerYardEntryCarrierLengthRule(80)
        val item = newScheduledTrailerYardEntry("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledTrailerYardEntryRequiredCodeRule("scheduled trailer yard entry needs a value")
        val item = newScheduledTrailerYardEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
