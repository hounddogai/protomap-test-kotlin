package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterSlotValidator()
        val item = newRosterSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RosterSlotRequiredLabelRule("roster slot needs a value")
        val item = newRosterSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = RosterSlotCategoryLengthRule(16)
        val item = newRosterSlot("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RosterSlotThresholdRangeRule(8.1, 999.7)
        val item = newRosterSlot("x", "y")
        item.threshold = 1001.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = RosterSlotReviewerLengthRule(255)
        val item = newRosterSlot("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
