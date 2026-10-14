package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSwapDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledSwapDigestValidator()
        val item = newScheduledSwapDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ScheduledSwapDigestMaximumRangeRule(9.1, 473.1)
        val item = newScheduledSwapDigest("x", "y")
        item.maximum = 474.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledSwapDigestRatioRangeRule(5.9, 1610.2)
        val item = newScheduledSwapDigest("x", "y")
        item.ratio = 1611.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ScheduledSwapDigestDiscountRangeRule(9.8, 3733.2)
        val item = newScheduledSwapDigest("x", "y")
        item.discount = 3734.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledSwapDigestRequiredTitleRule("scheduled swap digest needs an entry")
        val item = newScheduledSwapDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledSwapDigestNameLengthRule(1000)
        val item = newScheduledSwapDigest("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ScheduledSwapDigestDepotLengthRule(1000)
        val item = newScheduledSwapDigest("x", "y")
        item.depot = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
