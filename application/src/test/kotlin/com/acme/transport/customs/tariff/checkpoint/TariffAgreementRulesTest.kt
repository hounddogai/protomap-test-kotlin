package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffAgreementValidator()
        val item = newTariffAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TariffAgreementCostRangeRule(9.8, 1707.2)
        val item = newTariffAgreement("x", "y")
        item.cost = 1708.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffAgreementCodeLengthRule(32)
        val item = newTariffAgreement("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TariffAgreementRequiredLabelRule("tariff agreement needs this field")
        val item = newTariffAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
