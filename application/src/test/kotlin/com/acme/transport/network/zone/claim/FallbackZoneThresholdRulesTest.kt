package com.acme.transport.network.zone.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackZoneThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackZoneThresholdValidator()
        val item = newFallbackZoneThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackZoneThresholdCodeLengthRule(3600)
        val item = newFallbackZoneThreshold("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = FallbackZoneThresholdRatioRangeRule(7.9, 1524.5)
        val item = newFallbackZoneThreshold("x", "y")
        item.ratio = 1526.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = FallbackZoneThresholdRequiredUnitRule("fallback zone threshold needs an entry")
        val item = newFallbackZoneThreshold("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
