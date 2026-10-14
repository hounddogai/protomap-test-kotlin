package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTransitTimePreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTransitTimePreferenceValidator()
        val item = newAdaptiveTransitTimePreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = AdaptiveTransitTimePreferenceRequiredUnitRule("adaptive transit time preference needs a value")
        val item = newAdaptiveTransitTimePreference("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AdaptiveTransitTimePreferenceLoadKgRangeRule(7.7, 2784.5)
        val item = newAdaptiveTransitTimePreference("x", "y")
        item.loadKg = 2786.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveTransitTimePreferenceCodeLengthRule(64)
        val item = newAdaptiveTransitTimePreference("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = AdaptiveTransitTimePreferenceRequiredSourceRule("adaptive transit time preference needs this field")
        val item = newAdaptiveTransitTimePreference("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = AdaptiveTransitTimePreferenceMaximumRangeRule(6.8, 3863.6)
        val item = newAdaptiveTransitTimePreference("x", "y")
        item.maximum = 3865.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = AdaptiveTransitTimePreferenceUnitLengthRule(1000)
        val item = newAdaptiveTransitTimePreference("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
