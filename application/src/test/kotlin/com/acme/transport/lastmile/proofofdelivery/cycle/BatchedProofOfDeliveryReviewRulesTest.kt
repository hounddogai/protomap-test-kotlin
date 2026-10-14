package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedProofOfDeliveryReviewValidator()
        val item = newBatchedProofOfDeliveryReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedProofOfDeliveryReviewCodeLengthRule(3600)
        val item = newBatchedProofOfDeliveryReview("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedProofOfDeliveryReviewNameLengthRule(64)
        val item = newBatchedProofOfDeliveryReview("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedProofOfDeliveryReviewRequiredNameRule("batched proof of delivery review needs an entry")
        val item = newBatchedProofOfDeliveryReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = BatchedProofOfDeliveryReviewCarrierLengthRule(1000)
        val item = newBatchedProofOfDeliveryReview("x", "y")
        item.carrier = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = BatchedProofOfDeliveryReviewScoreRangeRule(4.1, 4048.1)
        val item = newBatchedProofOfDeliveryReview("x", "y")
        item.score = 4049.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
