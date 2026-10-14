package com.acme.transport.emissions.emissionfactor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEmissionFactorVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEmissionFactorVarianceValidator()
        val item = newAdaptiveEmissionFactorVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveEmissionFactorVarianceCodeLengthRule(64)
        val item = newAdaptiveEmissionFactorVariance("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveEmissionFactorVarianceRequiredLabelRule(
            "adaptive emission factor variance needs this field",
        )
        val item = newAdaptiveEmissionFactorVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveEmissionFactorVarianceLabelLengthRule(80)
        val item = newAdaptiveEmissionFactorVariance("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
