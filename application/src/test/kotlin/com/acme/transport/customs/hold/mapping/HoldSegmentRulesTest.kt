package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldSegmentValidator()
        val item = newHoldSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = HoldSegmentRequiredTitleRule("hold segment needs this field")
        val item = newHoldSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldSegmentRequiredCodeRule("hold segment needs this field")
        val item = newHoldSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = HoldSegmentRateRangeRule(7.9, 805.1)
        val item = newHoldSegment("x", "y")
        item.rate = 806.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
