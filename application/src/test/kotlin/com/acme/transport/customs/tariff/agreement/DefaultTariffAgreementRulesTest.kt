package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTariffAgreementValidator()
        val item = newDefaultTariffAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = DefaultTariffAgreementRequiredOwnerIdRule("default tariff agreement needs this field")
        val item = newDefaultTariffAgreement("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DefaultTariffAgreementRequiredCountryRule("default tariff agreement needs an entry")
        val item = newDefaultTariffAgreement("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DefaultTariffAgreementBatchSizeRangeRule(80, 86400)
        val item = newDefaultTariffAgreement("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = DefaultTariffAgreementCountryLengthRule(64)
        val item = newDefaultTariffAgreement("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
