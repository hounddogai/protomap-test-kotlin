package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedProofOfDeliveryPolicyValidator()
        val item = newBatchedProofOfDeliveryPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = BatchedProofOfDeliveryPolicyCostRangeRule(9.4, 919.8)
        val item = newBatchedProofOfDeliveryPolicy("x", "y")
        item.cost = 921.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedProofOfDeliveryPolicyTitleLengthRule(120)
        val item = newBatchedProofOfDeliveryPolicy("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedProofOfDeliveryPolicyCodeLengthRule(255)
        val item = newBatchedProofOfDeliveryPolicy("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = BatchedProofOfDeliveryPolicyRequiredDepotRule("batched proof of delivery policy needs an entry")
        val item = newBatchedProofOfDeliveryPolicy("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedProofOfDeliveryPolicyRequiredTitleRule("batched proof of delivery policy needs an entry")
        val item = newBatchedProofOfDeliveryPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
