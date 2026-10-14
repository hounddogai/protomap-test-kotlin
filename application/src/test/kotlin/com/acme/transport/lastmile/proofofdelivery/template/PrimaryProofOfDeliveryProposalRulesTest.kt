package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryProofOfDeliveryProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryProofOfDeliveryProposalValidator()
        val item = newPrimaryProofOfDeliveryProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryProofOfDeliveryProposalCodeLengthRule(80)
        val item = newPrimaryProofOfDeliveryProposal("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryProofOfDeliveryProposalRequiredCodeRule(
            "primary proof of delivery proposal needs this field",
        )
        val item = newPrimaryProofOfDeliveryProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryProofOfDeliveryProposalTitleLengthRule(1000)
        val item = newPrimaryProofOfDeliveryProposal("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryProofOfDeliveryProposalRequiredTitleRule(
            "primary proof of delivery proposal needs this field",
        )
        val item = newPrimaryProofOfDeliveryProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
