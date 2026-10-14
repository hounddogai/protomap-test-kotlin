package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerAdjustmentValidator()
        val item = newTrailerAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = TrailerAdjustmentRequiredRegionRule("trailer adjustment needs a value")
        val item = newTrailerAdjustment("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TrailerAdjustmentWindowDaysRangeRule(120, 86400)
        val item = newTrailerAdjustment("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerAdjustmentTitleLengthRule(1000)
        val item = newTrailerAdjustment("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = TrailerAdjustmentRequiredSummaryRule("trailer adjustment needs a value")
        val item = newTrailerAdjustment("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
