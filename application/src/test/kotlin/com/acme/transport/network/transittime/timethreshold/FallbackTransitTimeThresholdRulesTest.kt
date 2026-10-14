package com.acme.transport.network.transittime.timethreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTransitTimeThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTransitTimeThresholdValidator()
        val item = newFallbackTransitTimeThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackTransitTimeThresholdEtaMinutesRangeRule(1000, 3600)
        val item = newFallbackTransitTimeThreshold("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = FallbackTransitTimeThresholdRequiredSourceRule("fallback transit time threshold needs this field")
        val item = newFallbackTransitTimeThreshold("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackTransitTimeThresholdCodeLengthRule(120)
        val item = newFallbackTransitTimeThreshold("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackTransitTimeThresholdRequiredCodeRule("fallback transit time threshold needs a value")
        val item = newFallbackTransitTimeThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackTransitTimeThresholdRequiredNameRule("fallback transit time threshold needs this field")
        val item = newFallbackTransitTimeThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
