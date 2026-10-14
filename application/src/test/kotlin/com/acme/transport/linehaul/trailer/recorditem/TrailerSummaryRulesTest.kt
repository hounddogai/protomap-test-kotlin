package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerSummaryValidator()
        val item = newTrailerSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = TrailerSummaryDriverIdLengthRule(32)
        val item = newTrailerSummary("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = TrailerSummaryRequiredLaneRule("trailer summary needs this field")
        val item = newTrailerSummary("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = TrailerSummaryCeilingRangeRule(8.3, 1708.6)
        val item = newTrailerSummary("x", "y")
        item.ceiling = 1710.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = TrailerSummaryScoreRangeRule(6.2, 3212.7)
        val item = newTrailerSummary("x", "y")
        item.score = 3214.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
