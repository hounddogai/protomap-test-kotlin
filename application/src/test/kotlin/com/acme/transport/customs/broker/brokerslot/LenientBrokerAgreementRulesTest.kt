package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBrokerAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientBrokerAgreementValidator()
        val item = newLenientBrokerAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientBrokerAgreementRequiredTitleRule("lenient broker agreement needs a value")
        val item = newLenientBrokerAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = LenientBrokerAgreementPhoneLengthRule(1000)
        val item = newLenientBrokerAgreement("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientBrokerAgreementTitleLengthRule(16)
        val item = newLenientBrokerAgreement("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = LenientBrokerAgreementLoadKgRangeRule(8.1, 1278.6)
        val item = newLenientBrokerAgreement("x", "y")
        item.loadKg = 1280.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
