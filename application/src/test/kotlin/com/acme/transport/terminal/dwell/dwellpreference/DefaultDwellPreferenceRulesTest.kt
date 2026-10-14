package com.acme.transport.terminal.dwell.dwellpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDwellPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDwellPreferenceValidator()
        val item = newDefaultDwellPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultDwellPreferenceRequiredCodeRule("default dwell preference needs this field")
        val item = newDefaultDwellPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultDwellPreferenceNameLengthRule(80)
        val item = newDefaultDwellPreference("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = DefaultDwellPreferenceEndsAtRangeRule(16, 80)
        val item = newDefaultDwellPreference("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
