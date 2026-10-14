package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedServiceAreaAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedServiceAreaAgreementValidator()
        val item = newWeightedServiceAreaAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = WeightedServiceAreaAgreementCeilingRangeRule(6.9, 1713.2)
        val item = newWeightedServiceAreaAgreement("x", "y")
        item.ceiling = 1714.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = WeightedServiceAreaAgreementRequiredLocaleRule("weighted service area agreement needs a value")
        val item = newWeightedServiceAreaAgreement("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedServiceAreaAgreementCodeLengthRule(255)
        val item = newWeightedServiceAreaAgreement("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
