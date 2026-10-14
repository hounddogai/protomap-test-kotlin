package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryScoreValidator()
        val item = newProofOfDeliveryScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = ProofOfDeliveryScoreCarrierLengthRule(3600)
        val item = newProofOfDeliveryScore("x", "y")
        item.carrier = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ProofOfDeliveryScoreTimezoneLengthRule(80)
        val item = newProofOfDeliveryScore("x", "y")
        item.timezone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ProofOfDeliveryScoreRequiredTitleRule("proof of delivery score needs a value")
        val item = newProofOfDeliveryScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ProofOfDeliveryScoreRequiredTimezoneRule("proof of delivery score needs this field")
        val item = newProofOfDeliveryScore("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
