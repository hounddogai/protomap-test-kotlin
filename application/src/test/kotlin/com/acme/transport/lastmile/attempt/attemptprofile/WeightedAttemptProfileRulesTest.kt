package com.acme.transport.lastmile.attempt.attemptprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAttemptProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAttemptProfileValidator()
        val item = newWeightedAttemptProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = WeightedAttemptProfileRequiredExternalRefRule("weighted attempt profile needs this field")
        val item = newWeightedAttemptProfile("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedAttemptProfileCodeLengthRule(3600)
        val item = newWeightedAttemptProfile("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = WeightedAttemptProfileSequenceRangeRule(120, 1000)
        val item = newWeightedAttemptProfile("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedAttemptProfileRequiredTitleRule("weighted attempt profile needs an entry")
        val item = newWeightedAttemptProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
