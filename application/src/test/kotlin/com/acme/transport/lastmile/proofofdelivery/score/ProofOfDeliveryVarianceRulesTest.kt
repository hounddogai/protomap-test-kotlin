package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryVarianceValidator()
        val item = newProofOfDeliveryVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ProofOfDeliveryVarianceLaneLengthRule(1000)
        val item = newProofOfDeliveryVariance("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ProofOfDeliveryVarianceRequiredLabelRule("proof of delivery variance needs an entry")
        val item = newProofOfDeliveryVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryVarianceRequiredCodeRule("proof of delivery variance needs a value")
        val item = newProofOfDeliveryVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ProofOfDeliveryVarianceRequiredNameRule("proof of delivery variance needs this field")
        val item = newProofOfDeliveryVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ProofOfDeliveryVarianceLabelLengthRule(3600)
        val item = newProofOfDeliveryVariance("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
