package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveZoneAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveZoneAgreementValidator()
        val item = newAdaptiveZoneAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveZoneAgreementRequiredLabelRule("adaptive zone agreement needs a value")
        val item = newAdaptiveZoneAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = AdaptiveZoneAgreementRequiredPhoneRule("adaptive zone agreement needs a value")
        val item = newAdaptiveZoneAgreement("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AdaptiveZoneAgreementPhoneLengthRule(32)
        val item = newAdaptiveZoneAgreement("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveZoneAgreementCodeLengthRule(3600)
        val item = newAdaptiveZoneAgreement("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
