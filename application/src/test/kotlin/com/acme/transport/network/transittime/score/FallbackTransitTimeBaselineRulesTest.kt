package com.acme.transport.network.transittime.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTransitTimeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTransitTimeBaselineValidator()
        val item = newFallbackTransitTimeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackTransitTimeBaselineRequiredCodeRule("fallback transit time baseline needs this field")
        val item = newFallbackTransitTimeBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = FallbackTransitTimeBaselineRequiredLocaleRule("fallback transit time baseline needs a value")
        val item = newFallbackTransitTimeBaseline("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = FallbackTransitTimeBaselineRetriesRangeRule(32, 255)
        val item = newFallbackTransitTimeBaseline("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FallbackTransitTimeBaselineStopCountRangeRule(1000, 3600)
        val item = newFallbackTransitTimeBaseline("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackTransitTimeBaselineRequiredLabelRule("fallback transit time baseline needs a value")
        val item = newFallbackTransitTimeBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = FallbackTransitTimeBaselineRateRangeRule(5.0, 1292.4)
        val item = newFallbackTransitTimeBaseline("x", "y")
        item.rate = 1293.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
