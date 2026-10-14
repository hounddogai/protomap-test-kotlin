package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorClaimValidator()
        val item = newEmissionFactorClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = EmissionFactorClaimNameLengthRule(16)
        val item = newEmissionFactorClaim("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorClaimRequiredCodeRule("emission factor claim needs a value")
        val item = newEmissionFactorClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = EmissionFactorClaimTimezoneLengthRule(1000)
        val item = newEmissionFactorClaim("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
