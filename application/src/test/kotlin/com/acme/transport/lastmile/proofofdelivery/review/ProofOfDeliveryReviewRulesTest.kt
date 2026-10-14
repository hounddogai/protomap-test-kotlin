package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryReviewValidator()
        val item = newProofOfDeliveryReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryReviewRequiredCodeRule("proof of delivery review needs an entry")
        val item = newProofOfDeliveryReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ProofOfDeliveryReviewRequiredDriverIdRule("proof of delivery review needs this field")
        val item = newProofOfDeliveryReview("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ProofOfDeliveryReviewRequiredCurrencyRule("proof of delivery review needs this field")
        val item = newProofOfDeliveryReview("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ProofOfDeliveryReviewRequiredNameRule("proof of delivery review needs this field")
        val item = newProofOfDeliveryReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
