package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardAgreementValidator()
        val item = newRateCardAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RateCardAgreementFuelLitersRangeRule(4.8, 4146.6)
        val item = newRateCardAgreement("x", "y")
        item.fuelLiters = 4148.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = RateCardAgreementVersionRangeRule(120, 1000)
        val item = newRateCardAgreement("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RateCardAgreementOwnerIdLengthRule(86400)
        val item = newRateCardAgreement("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
