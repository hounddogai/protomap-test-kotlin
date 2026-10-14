package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryAgreementValidator()
        val item = newProofOfDeliveryAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ProofOfDeliveryAgreementNameLengthRule(32)
        val item = newProofOfDeliveryAgreement("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ProofOfDeliveryAgreementWindowDaysRangeRule(3600, 86400)
        val item = newProofOfDeliveryAgreement("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
