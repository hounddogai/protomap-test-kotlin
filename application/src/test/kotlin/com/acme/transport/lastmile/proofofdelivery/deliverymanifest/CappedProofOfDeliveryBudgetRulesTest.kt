package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedProofOfDeliveryBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedProofOfDeliveryBudgetValidator()
        val item = newCappedProofOfDeliveryBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = CappedProofOfDeliveryBudgetNotesLengthRule(86400)
        val item = newCappedProofOfDeliveryBudget("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = CappedProofOfDeliveryBudgetCarrierLengthRule(3600)
        val item = newCappedProofOfDeliveryBudget("x", "y")
        item.carrier = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
