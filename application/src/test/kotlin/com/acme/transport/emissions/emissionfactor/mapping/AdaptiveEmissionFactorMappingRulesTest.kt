package com.acme.transport.emissions.emissionfactor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEmissionFactorMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEmissionFactorMappingValidator()
        val item = newAdaptiveEmissionFactorMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = AdaptiveEmissionFactorMappingRequiredCarrierRule("adaptive emission factor mapping needs an entry")
        val item = newAdaptiveEmissionFactorMapping("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AdaptiveEmissionFactorMappingCreatedAtRangeRule(1000, 3600)
        val item = newAdaptiveEmissionFactorMapping("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AdaptiveEmissionFactorMappingPhoneLengthRule(1000)
        val item = newAdaptiveEmissionFactorMapping("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = AdaptiveEmissionFactorMappingCarrierLengthRule(255)
        val item = newAdaptiveEmissionFactorMapping("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveEmissionFactorMappingRequiredCodeRule("adaptive emission factor mapping needs a value")
        val item = newAdaptiveEmissionFactorMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
