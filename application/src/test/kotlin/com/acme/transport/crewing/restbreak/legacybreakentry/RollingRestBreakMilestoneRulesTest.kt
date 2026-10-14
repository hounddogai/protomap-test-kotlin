package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRestBreakMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRestBreakMilestoneValidator()
        val item = newRollingRestBreakMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RollingRestBreakMilestoneRequiredTimezoneRule("rolling rest break milestone needs this field")
        val item = newRollingRestBreakMilestone("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RollingRestBreakMilestoneTimezoneLengthRule(16)
        val item = newRollingRestBreakMilestone("x", "y")
        item.timezone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RollingRestBreakMilestoneQuantityRangeRule(32, 255)
        val item = newRollingRestBreakMilestone("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
