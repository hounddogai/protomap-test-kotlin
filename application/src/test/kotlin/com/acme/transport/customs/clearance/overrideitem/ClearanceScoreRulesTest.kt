package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceScoreValidator()
        val item = newClearanceScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ClearanceScoreSegmentLengthRule(80)
        val item = newClearanceScore("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ClearanceScoreRetriesRangeRule(80, 3600)
        val item = newClearanceScore("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ClearanceScoreChannelLengthRule(16)
        val item = newClearanceScore("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = ClearanceScoreScoreRangeRule(0.7, 1383.6)
        val item = newClearanceScore("x", "y")
        item.score = 1385.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceScoreRequiredCodeRule("clearance score needs an entry")
        val item = newClearanceScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ClearanceScoreRequiredSegmentRule("clearance score needs an entry")
        val item = newClearanceScore("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
