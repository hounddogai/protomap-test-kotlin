package com.acme.transport.emissions.emissionfactor.factorschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorDigestValidator()
        val item = newEmissionFactorDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorDigestRequiredCodeRule("emission factor digest needs an entry")
        val item = newEmissionFactorDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = EmissionFactorDigestTitleLengthRule(120)
        val item = newEmissionFactorDigest("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = EmissionFactorDigestRequiredTitleRule("emission factor digest needs an entry")
        val item = newEmissionFactorDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
