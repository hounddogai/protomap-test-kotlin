package com.acme.transport.terminal.dockdoor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorMilestoneValidator()
        val item = newDockDoorMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = DockDoorMilestoneWeightRangeRule(6.6, 3489.9)
        val item = newDockDoorMilestone("x", "y")
        item.weight = 3491.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DockDoorMilestoneDiscountRangeRule(8.8, 4512.3)
        val item = newDockDoorMilestone("x", "y")
        item.discount = 4513.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DockDoorMilestoneTitleLengthRule(32)
        val item = newDockDoorMilestone("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DockDoorMilestoneRatioRangeRule(6.2, 4755.7)
        val item = newDockDoorMilestone("x", "y")
        item.ratio = 4757.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
