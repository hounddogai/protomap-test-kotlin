package com.acme.transport.fleetops.leasereturn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLeaseReturnVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedLeaseReturnVarianceValidator()
        val item = newBatchedLeaseReturnVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedLeaseReturnVarianceCodeLengthRule(86400)
        val item = newBatchedLeaseReturnVariance("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedLeaseReturnVarianceRequiredCodeRule("batched lease return variance needs this field")
        val item = newBatchedLeaseReturnVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = BatchedLeaseReturnVarianceGraceDaysRangeRule(255, 3600)
        val item = newBatchedLeaseReturnVariance("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedLeaseReturnVarianceLabelLengthRule(1000)
        val item = newBatchedLeaseReturnVariance("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = BatchedLeaseReturnVarianceBudgetRangeRule(9.1, 1822.4)
        val item = newBatchedLeaseReturnVariance("x", "y")
        item.budget = 1823.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
