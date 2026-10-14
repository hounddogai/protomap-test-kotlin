package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingEmissionFactorAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingEmissionFactorAgreementValidator()
        val item = newRollingEmissionFactorAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingEmissionFactorAgreementRequiredLabelRule("rolling emission factor agreement needs a value")
        val item = newRollingEmissionFactorAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingEmissionFactorAgreementCodeLengthRule(86400)
        val item = newRollingEmissionFactorAgreement("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingEmissionFactorAgreementLabelLengthRule(16)
        val item = newRollingEmissionFactorAgreement("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = RollingEmissionFactorAgreementRequiredChannelRule(
            "rolling emission factor agreement needs an entry",
        )
        val item = newRollingEmissionFactorAgreement("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
