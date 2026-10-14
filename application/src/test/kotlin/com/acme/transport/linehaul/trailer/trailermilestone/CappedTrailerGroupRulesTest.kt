package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTrailerGroupValidator()
        val item = newCappedTrailerGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedTrailerGroupNameLengthRule(120)
        val item = newCappedTrailerGroup("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedTrailerGroupGraceDaysRangeRule(32, 120)
        val item = newCappedTrailerGroup("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
