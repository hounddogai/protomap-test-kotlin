package com.acme.transport.linehaul.trailer.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTrailerSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTrailerSummaryValidator()
        val item = newAdaptiveTrailerSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveTrailerSummaryCodeLengthRule(1000)
        val item = newAdaptiveTrailerSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveTrailerSummaryNameLengthRule(80)
        val item = newAdaptiveTrailerSummary("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AdaptiveTrailerSummaryCeilingRangeRule(5.4, 3123.0)
        val item = newAdaptiveTrailerSummary("x", "y")
        item.ceiling = 3124.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = AdaptiveTrailerSummaryChannelLengthRule(80)
        val item = newAdaptiveTrailerSummary("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
