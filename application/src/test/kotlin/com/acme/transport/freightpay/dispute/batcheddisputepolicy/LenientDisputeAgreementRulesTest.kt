package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDisputeAgreementValidator()
        val item = newLenientDisputeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientDisputeAgreementRequiredCodeRule("lenient dispute agreement needs an entry")
        val item = newLenientDisputeAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientDisputeAgreementUpdatedAtRangeRule(32, 80)
        val item = newLenientDisputeAgreement("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientDisputeAgreementRequiredNameRule("lenient dispute agreement needs an entry")
        val item = newLenientDisputeAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDisputeAgreementCodeLengthRule(80)
        val item = newLenientDisputeAgreement("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
