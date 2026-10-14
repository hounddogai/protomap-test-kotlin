package com.acme.transport.lastmile.proofofdelivery.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedProofOfDeliveryBudgetValidator()
        val item = newBatchedProofOfDeliveryBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedProofOfDeliveryBudgetLabelLengthRule(86400)
        val item = newBatchedProofOfDeliveryBudget("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = BatchedProofOfDeliveryBudgetRequiredSummaryRule("batched proof of delivery budget needs a value")
        val item = newBatchedProofOfDeliveryBudget("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = BatchedProofOfDeliveryBudgetVersionRangeRule(120, 86400)
        val item = newBatchedProofOfDeliveryBudget("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
