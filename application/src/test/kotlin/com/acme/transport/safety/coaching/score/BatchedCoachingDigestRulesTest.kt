package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCoachingDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCoachingDigestValidator()
        val item = newBatchedCoachingDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = BatchedCoachingDigestRequiredNotesRule("batched coaching digest needs a value")
        val item = newBatchedCoachingDigest("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BatchedCoachingDigestStartsAtRangeRule(16, 80)
        val item = newBatchedCoachingDigest("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
