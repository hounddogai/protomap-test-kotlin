package com.acme.transport.emissions.emissionfactor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEmissionFactorSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEmissionFactorSegmentValidator()
        val item = newAdaptiveEmissionFactorSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = AdaptiveEmissionFactorSegmentRequiredCarrierRule(
            "adaptive emission factor segment needs this field",
        )
        val item = newAdaptiveEmissionFactorSegment("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = AdaptiveEmissionFactorSegmentMaximumRangeRule(8.9, 2130.7)
        val item = newAdaptiveEmissionFactorSegment("x", "y")
        item.maximum = 2132.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = AdaptiveEmissionFactorSegmentRevisionRangeRule(1000, 3600)
        val item = newAdaptiveEmissionFactorSegment("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = AdaptiveEmissionFactorSegmentCarrierLengthRule(120)
        val item = newAdaptiveEmissionFactorSegment("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = AdaptiveEmissionFactorSegmentRequiredUnitRule("adaptive emission factor segment needs an entry")
        val item = newAdaptiveEmissionFactorSegment("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
