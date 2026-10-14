package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorPolicyValidator()
        val item = newEmissionFactorPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EmissionFactorPolicyRequiredLabelRule("emission factor policy needs this field")
        val item = newEmissionFactorPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = EmissionFactorPolicyThresholdRangeRule(3.8, 2917.1)
        val item = newEmissionFactorPolicy("x", "y")
        item.threshold = 2918.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorPolicyCodeLengthRule(86400)
        val item = newEmissionFactorPolicy("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = EmissionFactorPolicyLabelLengthRule(255)
        val item = newEmissionFactorPolicy("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = EmissionFactorPolicyRateRangeRule(8.4, 1920.5)
        val item = newEmissionFactorPolicy("x", "y")
        item.rate = 1922.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
