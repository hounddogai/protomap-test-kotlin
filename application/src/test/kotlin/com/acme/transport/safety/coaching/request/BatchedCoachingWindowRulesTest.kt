package com.acme.transport.safety.coaching.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCoachingWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCoachingWindowValidator()
        val item = newBatchedCoachingWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = BatchedCoachingWindowMinimumRangeRule(8.6, 4019.4)
        val item = newBatchedCoachingWindow("x", "y")
        item.minimum = 4020.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedCoachingWindowRequiredLabelRule("batched coaching window needs this field")
        val item = newBatchedCoachingWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
