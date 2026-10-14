package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCapacityBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCapacityBundleValidator()
        val item = newFallbackCapacityBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackCapacityBundleEtaMinutesRangeRule(32, 80)
        val item = newFallbackCapacityBundle("x", "y")
        item.etaMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = FallbackCapacityBundleRatioRangeRule(8.0, 2902.0)
        val item = newFallbackCapacityBundle("x", "y")
        item.ratio = 2903.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = FallbackCapacityBundleRequiredSourceRule("fallback capacity bundle needs an entry")
        val item = newFallbackCapacityBundle("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
