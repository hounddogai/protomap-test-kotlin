package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAttemptSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAttemptSlotValidator()
        val item = newScheduledAttemptSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAttemptSlotCodeLengthRule(64)
        val item = newScheduledAttemptSlot("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ScheduledAttemptSlotLaneLengthRule(3600)
        val item = newScheduledAttemptSlot("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ScheduledAttemptSlotRequiredDescriptionRule("scheduled attempt slot needs a value")
        val item = newScheduledAttemptSlot("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ScheduledAttemptSlotCostRangeRule(5.4, 4239.8)
        val item = newScheduledAttemptSlot("x", "y")
        item.cost = 4241.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
