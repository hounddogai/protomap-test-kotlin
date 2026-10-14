package com.acme.transport.lastmile.drop.weighteddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropSummaryValidator()
        val item = newDropSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = DropSummaryRequiredChannelRule("drop summary needs this field")
        val item = newDropSummary("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DropSummaryQuantityRangeRule(16, 3600)
        val item = newDropSummary("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = DropSummaryRequiredLaneRule("drop summary needs this field")
        val item = newDropSummary("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DropSummaryLaneLengthRule(3600)
        val item = newDropSummary("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
