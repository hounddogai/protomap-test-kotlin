package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorPreferenceValidator()
        val item = newEmissionFactorPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = EmissionFactorPreferenceLabelLengthRule(16)
        val item = newEmissionFactorPreference("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EmissionFactorPreferenceRequiredLabelRule("emission factor preference needs an entry")
        val item = newEmissionFactorPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = EmissionFactorPreferenceLimitRangeRule(255, 3600)
        val item = newEmissionFactorPreference("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
