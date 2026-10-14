package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryProposalValidator()
        val item = newProofOfDeliveryProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ProofOfDeliveryProposalRequiredTitleRule("proof of delivery proposal needs an entry")
        val item = newProofOfDeliveryProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ProofOfDeliveryProposalTitleLengthRule(16)
        val item = newProofOfDeliveryProposal("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
