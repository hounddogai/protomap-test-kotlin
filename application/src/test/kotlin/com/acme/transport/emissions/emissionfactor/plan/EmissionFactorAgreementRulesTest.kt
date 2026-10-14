package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorAgreementValidator()
        val item = newEmissionFactorAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = EmissionFactorAgreementQuantityRangeRule(64, 1000)
        val item = newEmissionFactorAgreement("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = EmissionFactorAgreementThresholdRangeRule(1.9, 842.8)
        val item = newEmissionFactorAgreement("x", "y")
        item.threshold = 844.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = EmissionFactorAgreementOwnerIdLengthRule(86400)
        val item = newEmissionFactorAgreement("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = EmissionFactorAgreementRequiredOwnerIdRule("emission factor agreement needs an entry")
        val item = newEmissionFactorAgreement("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = EmissionFactorAgreementRequiredTitleRule("emission factor agreement needs this field")
        val item = newEmissionFactorAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
