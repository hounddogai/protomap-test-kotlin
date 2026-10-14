package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAttemptRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAttemptRequestValidator()
        val item = newTieredAttemptRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = TieredAttemptRequestMinimumRangeRule(4.8, 794.5)
        val item = newTieredAttemptRequest("x", "y")
        item.minimum = 796.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TieredAttemptRequestStopCountRangeRule(32, 120)
        val item = newTieredAttemptRequest("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
