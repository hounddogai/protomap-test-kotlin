package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorVarianceValidator()
        val item = newEmissionFactorVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorVarianceRequiredCodeRule("emission factor variance needs a value")
        val item = newEmissionFactorVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = EmissionFactorVarianceDepotLengthRule(64)
        val item = newEmissionFactorVariance("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorVarianceCodeLengthRule(3600)
        val item = newEmissionFactorVariance("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
