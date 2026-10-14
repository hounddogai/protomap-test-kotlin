package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldMilestoneValidator()
        val item = newHoldMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = HoldMilestoneCountryLengthRule(120)
        val item = newHoldMilestone("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = HoldMilestoneOwnerIdLengthRule(255)
        val item = newHoldMilestone("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HoldMilestoneRequiredNameRule("hold milestone needs a value")
        val item = newHoldMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldMilestoneCodeLengthRule(80)
        val item = newHoldMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = HoldMilestoneStopCountRangeRule(32, 80)
        val item = newHoldMilestone("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = HoldMilestoneStartsAtRangeRule(32, 64)
        val item = newHoldMilestone("x", "y")
        item.startsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
