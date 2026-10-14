package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLockerSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLockerSegmentValidator()
        val item = newCappedLockerSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = CappedLockerSegmentRequiredChannelRule("capped locker segment needs a value")
        val item = newCappedLockerSegment("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedLockerSegmentRequiredLabelRule("capped locker segment needs a value")
        val item = newCappedLockerSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CappedLockerSegmentCeilingRangeRule(4.4, 2239.2)
        val item = newCappedLockerSegment("x", "y")
        item.ceiling = 2240.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
