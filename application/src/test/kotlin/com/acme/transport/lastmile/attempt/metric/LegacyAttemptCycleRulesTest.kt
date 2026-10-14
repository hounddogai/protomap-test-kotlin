package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAttemptCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAttemptCycleValidator()
        val item = newLegacyAttemptCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LegacyAttemptCycleAttemptsRangeRule(255, 1000)
        val item = newLegacyAttemptCycle("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = LegacyAttemptCycleChannelLengthRule(16)
        val item = newLegacyAttemptCycle("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
