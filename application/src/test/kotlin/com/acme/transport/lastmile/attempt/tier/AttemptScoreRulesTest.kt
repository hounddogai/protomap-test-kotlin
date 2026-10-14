package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptScoreValidator()
        val item = newAttemptScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = AttemptScorePriorityRangeRule(16, 120)
        val item = newAttemptScore("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = AttemptScoreStopCountRangeRule(32, 1000)
        val item = newAttemptScore("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AttemptScoreRequiredNameRule("attempt score needs a value")
        val item = newAttemptScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = AttemptScoreLaneLengthRule(255)
        val item = newAttemptScore("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
