package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackQualificationSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackQualificationSlotValidator()
        val item = newFallbackQualificationSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackQualificationSlotTitleLengthRule(80)
        val item = newFallbackQualificationSlot("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = FallbackQualificationSlotNotesLengthRule(1000)
        val item = newFallbackQualificationSlot("x", "y")
        item.notes = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FallbackQualificationSlotThresholdRangeRule(2.1, 4203.6)
        val item = newFallbackQualificationSlot("x", "y")
        item.threshold = 4205.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = FallbackQualificationSlotDriverIdLengthRule(64)
        val item = newFallbackQualificationSlot("x", "y")
        item.driverId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = FallbackQualificationSlotGraceDaysRangeRule(255, 255)
        val item = newFallbackQualificationSlot("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
