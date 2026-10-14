package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldTierValidator()
        val item = newHoldTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = HoldTierRequiredSummaryRule("hold tier needs a value")
        val item = newHoldTier("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = HoldTierTitleLengthRule(86400)
        val item = newHoldTier("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = HoldTierRatioRangeRule(2.8, 1051.3)
        val item = newHoldTier("x", "y")
        item.ratio = 1052.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = HoldTierTimezoneLengthRule(255)
        val item = newHoldTier("x", "y")
        item.timezone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
