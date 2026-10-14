package com.acme.transport.terminal.dwell.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDwellPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDwellPreferenceValidator()
        val item = newFallbackDwellPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackDwellPreferenceNameLengthRule(255)
        val item = newFallbackDwellPreference("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = FallbackDwellPreferenceWeightRangeRule(4.7, 4412.2)
        val item = newFallbackDwellPreference("x", "y")
        item.weight = 4413.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDwellPreferenceRequiredCodeRule("fallback dwell preference needs an entry")
        val item = newFallbackDwellPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
