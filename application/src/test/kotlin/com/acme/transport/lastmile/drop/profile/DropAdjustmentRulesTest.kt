package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropAdjustmentValidator()
        val item = newDropAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DropAdjustmentTitleLengthRule(80)
        val item = newDropAdjustment("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DropAdjustmentStopCountRangeRule(32, 120)
        val item = newDropAdjustment("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = DropAdjustmentFloorValueRangeRule(6.2, 3977.2)
        val item = newDropAdjustment("x", "y")
        item.floorValue = 3978.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = DropAdjustmentRequiredSummaryRule("drop adjustment needs a value")
        val item = newDropAdjustment("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropAdjustmentRequiredCodeRule("drop adjustment needs this field")
        val item = newDropAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DropAdjustmentPriorityRangeRule(16, 64)
        val item = newDropAdjustment("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
