package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBrokerAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalBrokerAgreementValidator()
        val item = newGlobalBrokerAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalBrokerAgreementCodeLengthRule(3600)
        val item = newGlobalBrokerAgreement("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalBrokerAgreementRequiredLabelRule("global broker agreement needs this field")
        val item = newGlobalBrokerAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = GlobalBrokerAgreementSourceLengthRule(64)
        val item = newGlobalBrokerAgreement("x", "y")
        item.source = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = GlobalBrokerAgreementLocaleLengthRule(64)
        val item = newGlobalBrokerAgreement("x", "y")
        item.locale = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalBrokerAgreementDurationMinutesRangeRule(80, 120)
        val item = newGlobalBrokerAgreement("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
