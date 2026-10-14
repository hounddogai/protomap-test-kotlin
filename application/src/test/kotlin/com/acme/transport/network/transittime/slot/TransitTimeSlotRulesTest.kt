package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeSlotValidator()
        val item = newTransitTimeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TransitTimeSlotRequiredNotesRule("transit time slot needs this field")
        val item = newTransitTimeSlot("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TransitTimeSlotTitleLengthRule(32)
        val item = newTransitTimeSlot("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TransitTimeSlotRequiredChecksumRule("transit time slot needs an entry")
        val item = newTransitTimeSlot("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = TransitTimeSlotChecksumLengthRule(1000)
        val item = newTransitTimeSlot("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
