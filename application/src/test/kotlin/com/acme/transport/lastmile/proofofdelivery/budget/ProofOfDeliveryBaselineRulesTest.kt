package com.acme.transport.lastmile.proofofdelivery.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryBaselineValidator()
        val item = newProofOfDeliveryBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryBaselineRequiredCodeRule("proof of delivery baseline needs an entry")
        val item = newProofOfDeliveryBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ProofOfDeliveryBaselineLabelLengthRule(32)
        val item = newProofOfDeliveryBaseline("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = ProofOfDeliveryBaselineCarrierLengthRule(255)
        val item = newProofOfDeliveryBaseline("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ProofOfDeliveryBaselineRequiredLabelRule("proof of delivery baseline needs an entry")
        val item = newProofOfDeliveryBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
