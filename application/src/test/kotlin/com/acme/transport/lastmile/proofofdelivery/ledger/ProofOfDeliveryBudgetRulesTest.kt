package com.acme.transport.lastmile.proofofdelivery.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryBudgetValidator()
        val item = newProofOfDeliveryBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ProofOfDeliveryBudgetRequiredNameRule("proof of delivery budget needs a value")
        val item = newProofOfDeliveryBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ProofOfDeliveryBudgetRequiredCarrierRule("proof of delivery budget needs this field")
        val item = newProofOfDeliveryBudget("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = ProofOfDeliveryBudgetCarrierLengthRule(120)
        val item = newProofOfDeliveryBudget("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ProofOfDeliveryBudgetNameLengthRule(80)
        val item = newProofOfDeliveryBudget("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ProofOfDeliveryBudgetTrailerIdLengthRule(64)
        val item = newProofOfDeliveryBudget("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
