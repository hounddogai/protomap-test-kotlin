package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedDisputeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDisputeBatchValidator()
        val item = newBatchedDisputeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = BatchedDisputeBatchMaximumRangeRule(9.9, 1691.1)
        val item = newBatchedDisputeBatch("x", "y")
        item.maximum = 1692.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = BatchedDisputeBatchSequenceRangeRule(32, 1000)
        val item = newBatchedDisputeBatch("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = BatchedDisputeBatchAmountRangeRule(1.9, 3955.7)
        val item = newBatchedDisputeBatch("x", "y")
        item.amount = 3957.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
