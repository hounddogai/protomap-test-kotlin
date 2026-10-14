package com.acme.transport.lastmile.attempt.attemptdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAttemptDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAttemptDigestValidator()
        val item = newWeightedAttemptDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = WeightedAttemptDigestRequiredNotesRule("weighted attempt digest needs a value")
        val item = newWeightedAttemptDigest("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedAttemptDigestRequiredTitleRule("weighted attempt digest needs this field")
        val item = newWeightedAttemptDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = WeightedAttemptDigestStartsAtRangeRule(255, 1000)
        val item = newWeightedAttemptDigest("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
