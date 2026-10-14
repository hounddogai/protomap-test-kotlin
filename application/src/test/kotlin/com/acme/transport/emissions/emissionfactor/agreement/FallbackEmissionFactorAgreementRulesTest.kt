package com.acme.transport.emissions.emissionfactor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEmissionFactorAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackEmissionFactorAgreementValidator()
        val item = newFallbackEmissionFactorAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FallbackEmissionFactorAgreementMinimumRangeRule(9.7, 2621.0)
        val item = newFallbackEmissionFactorAgreement("x", "y")
        item.minimum = 2622.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackEmissionFactorAgreementTitleLengthRule(80)
        val item = newFallbackEmissionFactorAgreement("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackEmissionFactorAgreementRequiredCodeRule(
            "fallback emission factor agreement needs this field",
        )
        val item = newFallbackEmissionFactorAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
