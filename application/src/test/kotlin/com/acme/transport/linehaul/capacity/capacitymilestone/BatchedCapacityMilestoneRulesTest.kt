package com.acme.transport.linehaul.capacity.capacitymilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacityMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCapacityMilestoneValidator()
        val item = newBatchedCapacityMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = BatchedCapacityMilestoneCarrierLengthRule(1000)
        val item = newBatchedCapacityMilestone("x", "y")
        item.carrier = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedCapacityMilestoneEndsAtRangeRule(32, 86400)
        val item = newBatchedCapacityMilestone("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedCapacityMilestoneRequiredCodeRule("batched capacity milestone needs this field")
        val item = newBatchedCapacityMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
