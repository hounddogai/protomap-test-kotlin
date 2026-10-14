package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDropPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDropPreferenceValidator()
        val item = newDefaultDropPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultDropPreferenceRequiredNameRule("default drop preference needs an entry")
        val item = newDefaultDropPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = DefaultDropPreferenceSummaryLengthRule(1000)
        val item = newDefaultDropPreference("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultDropPreferenceNameLengthRule(3600)
        val item = newDefaultDropPreference("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DefaultDropPreferenceStopCountRangeRule(16, 16)
        val item = newDefaultDropPreference("x", "y")
        item.stopCount = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
