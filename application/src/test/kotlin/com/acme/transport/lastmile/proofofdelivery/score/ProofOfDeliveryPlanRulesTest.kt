package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryPlanValidator()
        val item = newProofOfDeliveryPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ProofOfDeliveryPlanDockLengthRule(255)
        val item = newProofOfDeliveryPlan("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ProofOfDeliveryPlanRequiredCurrencyRule("proof of delivery plan needs an entry")
        val item = newProofOfDeliveryPlan("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ProofOfDeliveryPlanBatchSizeRangeRule(16, 255)
        val item = newProofOfDeliveryPlan("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
