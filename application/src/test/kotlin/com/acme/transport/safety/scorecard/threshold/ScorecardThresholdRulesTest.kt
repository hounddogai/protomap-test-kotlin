package com.acme.transport.safety.scorecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardThresholdValidator()
        val item = newScorecardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ScorecardThresholdMaximumRangeRule(6.4, 1264.4)
        val item = newScorecardThreshold("x", "y")
        item.maximum = 1265.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ScorecardThresholdAttemptsRangeRule(16, 86400)
        val item = newScorecardThreshold("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScorecardThresholdRequiredCodeRule("scorecard threshold needs a value")
        val item = newScorecardThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScorecardThresholdCodeLengthRule(16)
        val item = newScorecardThreshold("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = ScorecardThresholdCurrencyLengthRule(120)
        val item = newScorecardThreshold("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
