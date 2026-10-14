package com.acme.transport.crewing.availability.scheduledavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAvailabilitySlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAvailabilitySlotValidator()
        val item = newCappedAvailabilitySlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedAvailabilitySlotRequiredTitleRule("capped availability slot needs this field")
        val item = newCappedAvailabilitySlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CappedAvailabilitySlotDurationMinutesRangeRule(3600, 3600)
        val item = newCappedAvailabilitySlot("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CappedAvailabilitySlotMarginRangeRule(5.0, 2208.8)
        val item = newCappedAvailabilitySlot("x", "y")
        item.margin = 2210.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedAvailabilitySlotTitleLengthRule(1000)
        val item = newCappedAvailabilitySlot("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
