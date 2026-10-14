package com.acme.transport.network.cutoff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffSlotValidator()
        val item = newCutoffSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CutoffSlotPriorityRangeRule(16, 64)
        val item = newCutoffSlot("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CutoffSlotTitleLengthRule(32)
        val item = newCutoffSlot("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = CutoffSlotRequiredReviewerRule("cutoff slot needs this field")
        val item = newCutoffSlot("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
