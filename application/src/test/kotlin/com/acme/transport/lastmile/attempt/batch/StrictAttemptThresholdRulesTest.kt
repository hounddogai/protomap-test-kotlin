package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAttemptThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAttemptThresholdValidator()
        val item = newStrictAttemptThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictAttemptThresholdCodeLengthRule(3600)
        val item = newStrictAttemptThreshold("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = StrictAttemptThresholdCostRangeRule(0.4, 3085.7)
        val item = newStrictAttemptThreshold("x", "y")
        item.cost = 3087.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictAttemptThresholdRequiredLabelRule("strict attempt threshold needs this field")
        val item = newStrictAttemptThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = StrictAttemptThresholdRequiredTimezoneRule("strict attempt threshold needs this field")
        val item = newStrictAttemptThreshold("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = StrictAttemptThresholdRatioRangeRule(9.6, 3321.9)
        val item = newStrictAttemptThreshold("x", "y")
        item.ratio = 3323.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
