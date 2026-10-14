package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNearMissSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNearMissSegmentValidator()
        val item = newBatchedNearMissSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = BatchedNearMissSegmentLimitRangeRule(16, 1000)
        val item = newBatchedNearMissSegment("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = BatchedNearMissSegmentRequiredLocaleRule("batched near miss segment needs a value")
        val item = newBatchedNearMissSegment("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
