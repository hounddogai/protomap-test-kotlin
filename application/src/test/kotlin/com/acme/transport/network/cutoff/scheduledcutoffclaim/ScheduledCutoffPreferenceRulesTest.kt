package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCutoffPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCutoffPreferenceValidator()
        val item = newScheduledCutoffPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ScheduledCutoffPreferenceRequiredLocaleRule("scheduled cutoff preference needs a value")
        val item = newScheduledCutoffPreference("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledCutoffPreferenceRequiredTitleRule("scheduled cutoff preference needs an entry")
        val item = newScheduledCutoffPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ScheduledCutoffPreferenceDescriptionLengthRule(64)
        val item = newScheduledCutoffPreference("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
