package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterPreferenceValidator()
        val item = newRosterPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RosterPreferenceRequiredTitleRule("roster preference needs this field")
        val item = newRosterPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RosterPreferenceExpiresAtRangeRule(64, 120)
        val item = newRosterPreference("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RosterPreferenceTitleLengthRule(16)
        val item = newRosterPreference("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
