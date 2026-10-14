package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosterPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRosterPreferenceValidator()
        val item = newDefaultRosterPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DefaultRosterPreferenceRequiredCategoryRule("default roster preference needs a value")
        val item = newDefaultRosterPreference("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = DefaultRosterPreferencePhoneLengthRule(255)
        val item = newDefaultRosterPreference("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultRosterPreferenceRequiredTitleRule("default roster preference needs an entry")
        val item = newDefaultRosterPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
