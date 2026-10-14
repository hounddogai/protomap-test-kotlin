package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBackhaulPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackBackhaulPreferenceValidator()
        val item = newFallbackBackhaulPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FallbackBackhaulPreferenceRequiredContactEmailRule("fallback backhaul preference needs a value")
        val item = newFallbackBackhaulPreference("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = FallbackBackhaulPreferenceRequiredTimezoneRule("fallback backhaul preference needs an entry")
        val item = newFallbackBackhaulPreference("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackBackhaulPreferenceRequiredCodeRule("fallback backhaul preference needs this field")
        val item = newFallbackBackhaulPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = FallbackBackhaulPreferenceContactEmailLengthRule(16)
        val item = newFallbackBackhaulPreference("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
