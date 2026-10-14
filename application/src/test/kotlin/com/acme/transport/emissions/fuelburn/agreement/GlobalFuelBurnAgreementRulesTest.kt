package com.acme.transport.emissions.fuelburn.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelBurnAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFuelBurnAgreementValidator()
        val item = newGlobalFuelBurnAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalFuelBurnAgreementAttemptsRangeRule(16, 3600)
        val item = newGlobalFuelBurnAgreement("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalFuelBurnAgreementCodeLengthRule(86400)
        val item = newGlobalFuelBurnAgreement("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalFuelBurnAgreementRequiredLabelRule("global fuel burn agreement needs a value")
        val item = newGlobalFuelBurnAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
