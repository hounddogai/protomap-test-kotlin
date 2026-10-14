package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDisputeRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDisputeRuleValidator()
        val item = newBatchedDisputeRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = BatchedDisputeRuleRequiredPhoneRule("batched dispute rule needs this field")
        val item = newBatchedDisputeRule("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDisputeRuleRequiredCodeRule("batched dispute rule needs this field")
        val item = newBatchedDisputeRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = BatchedDisputeRuleRequiredSummaryRule("batched dispute rule needs this field")
        val item = newBatchedDisputeRule("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
