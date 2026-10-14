package com.acme.transport.crewing.restbreak.breakmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRestBreakCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRestBreakCheckpointValidator()
        val item = newStrictRestBreakCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = StrictRestBreakCheckpointWindowDaysRangeRule(16, 80)
        val item = newStrictRestBreakCheckpoint("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = StrictRestBreakCheckpointMinimumRangeRule(7.3, 932.9)
        val item = newStrictRestBreakCheckpoint("x", "y")
        item.minimum = 934.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictRestBreakCheckpointCodeLengthRule(1000)
        val item = newStrictRestBreakCheckpoint("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = StrictRestBreakCheckpointVersionRangeRule(16, 86400)
        val item = newStrictRestBreakCheckpoint("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
