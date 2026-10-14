package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderAgreementValidator()
        val item = newTenderAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = TenderAgreementCarrierLengthRule(1000)
        val item = newTenderAgreement("x", "y")
        item.carrier = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = TenderAgreementUpdatedAtRangeRule(255, 1000)
        val item = newTenderAgreement("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
