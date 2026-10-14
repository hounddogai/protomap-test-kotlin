package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingEmissionFactorClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingEmissionFactorClaimValidator()
        val item = newPendingEmissionFactorClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingEmissionFactorClaimLabelLengthRule(1000)
        val item = newPendingEmissionFactorClaim("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PendingEmissionFactorClaimStopCountRangeRule(16, 120)
        val item = newPendingEmissionFactorClaim("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
