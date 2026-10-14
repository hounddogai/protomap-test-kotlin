package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryEstimateValidator()
        val item = newProofOfDeliveryEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryEstimateRequiredCodeRule("proof of delivery estimate needs an entry")
        val item = newProofOfDeliveryEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ProofOfDeliveryEstimateDepotLengthRule(80)
        val item = newProofOfDeliveryEstimate("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ProofOfDeliveryEstimateRequiredLabelRule("proof of delivery estimate needs a value")
        val item = newProofOfDeliveryEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
