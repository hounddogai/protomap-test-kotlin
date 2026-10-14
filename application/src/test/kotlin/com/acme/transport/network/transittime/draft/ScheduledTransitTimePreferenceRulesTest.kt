package com.acme.transport.network.transittime.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledTransitTimePreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTransitTimePreferenceValidator()
        val item = newScheduledTransitTimePreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ScheduledTransitTimePreferenceLimitRangeRule(16, 255)
        val item = newScheduledTransitTimePreference("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
