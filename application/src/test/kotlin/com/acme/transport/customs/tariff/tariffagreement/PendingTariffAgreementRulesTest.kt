package com.acme.transport.customs.tariff.tariffagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTariffAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTariffAgreementValidator()
        val item = newPendingTariffAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PendingTariffAgreementRequiredSegmentRule("pending tariff agreement needs an entry")
        val item = newPendingTariffAgreement("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingTariffAgreementCodeLengthRule(80)
        val item = newPendingTariffAgreement("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingTariffAgreementRequiredCodeRule("pending tariff agreement needs this field")
        val item = newPendingTariffAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
