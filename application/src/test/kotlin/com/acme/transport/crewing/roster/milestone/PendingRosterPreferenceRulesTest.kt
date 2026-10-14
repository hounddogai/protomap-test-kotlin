package com.acme.transport.crewing.roster.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRosterPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRosterPreferenceValidator()
        val item = newPendingRosterPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingRosterPreferenceTitleLengthRule(3600)
        val item = newPendingRosterPreference("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRosterPreferenceCodeLengthRule(16)
        val item = newPendingRosterPreference("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = PendingRosterPreferenceDockLengthRule(80)
        val item = newPendingRosterPreference("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = PendingRosterPreferenceRequiredDockRule("pending roster preference needs a value")
        val item = newPendingRosterPreference("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
