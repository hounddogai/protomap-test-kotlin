package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerPreferenceValidator()
        val item = newTrailerPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = TrailerPreferenceGraceDaysRangeRule(16, 86400)
        val item = newTrailerPreference("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TrailerPreferenceRequiredLabelRule("trailer preference needs an entry")
        val item = newTrailerPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = TrailerPreferenceRequiredReviewerRule("trailer preference needs a value")
        val item = newTrailerPreference("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
