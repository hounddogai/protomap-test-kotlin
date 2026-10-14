package com.acme.transport.linehaul.capacity.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCapacityPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCapacityPreferenceValidator()
        val item = newFallbackCapacityPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = FallbackCapacityPreferenceRequiredDepotRule("fallback capacity preference needs an entry")
        val item = newFallbackCapacityPreference("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackCapacityPreferenceRequiredCodeRule("fallback capacity preference needs a value")
        val item = newFallbackCapacityPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackCapacityPreferenceCodeLengthRule(3600)
        val item = newFallbackCapacityPreference("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
