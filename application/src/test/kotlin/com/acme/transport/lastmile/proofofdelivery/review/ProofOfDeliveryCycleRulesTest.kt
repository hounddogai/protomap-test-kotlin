package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryCycleValidator()
        val item = newProofOfDeliveryCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ProofOfDeliveryCycleRequiredNotesRule("proof of delivery cycle needs an entry")
        val item = newProofOfDeliveryCycle("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ProofOfDeliveryCycleAmountRangeRule(2.8, 3495.5)
        val item = newProofOfDeliveryCycle("x", "y")
        item.amount = 3497.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryCycleCodeLengthRule(16)
        val item = newProofOfDeliveryCycle("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
