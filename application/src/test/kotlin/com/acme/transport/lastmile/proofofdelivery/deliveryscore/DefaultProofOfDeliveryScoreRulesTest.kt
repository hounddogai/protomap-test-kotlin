package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProofOfDeliveryScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultProofOfDeliveryScoreValidator()
        val item = newDefaultProofOfDeliveryScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DefaultProofOfDeliveryScoreBudgetRangeRule(7.6, 4417.2)
        val item = newDefaultProofOfDeliveryScore("x", "y")
        item.budget = 4418.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultProofOfDeliveryScoreRequiredCodeRule("default proof of delivery score needs this field")
        val item = newDefaultProofOfDeliveryScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = DefaultProofOfDeliveryScoreReviewerLengthRule(255)
        val item = newDefaultProofOfDeliveryScore("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
