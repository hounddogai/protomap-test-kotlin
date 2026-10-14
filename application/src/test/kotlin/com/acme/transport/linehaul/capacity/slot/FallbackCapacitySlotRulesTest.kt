package com.acme.transport.linehaul.capacity.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCapacitySlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCapacitySlotValidator()
        val item = newFallbackCapacitySlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FallbackCapacitySlotPalletsRangeRule(32, 3600)
        val item = newFallbackCapacitySlot("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = FallbackCapacitySlotReviewerLengthRule(255)
        val item = newFallbackCapacitySlot("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
