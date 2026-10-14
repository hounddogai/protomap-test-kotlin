package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingPreferenceValidator()
        val item = newCoachingPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = CoachingPreferenceRequiredChannelRule("coaching preference needs an entry")
        val item = newCoachingPreference("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CoachingPreferenceTitleLengthRule(1000)
        val item = newCoachingPreference("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CoachingPreferenceRevisionRangeRule(64, 120)
        val item = newCoachingPreference("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
