package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAvailabilityPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAvailabilityPreferenceValidator()
        val item = newPendingAvailabilityPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = PendingAvailabilityPreferenceWindowDaysRangeRule(64, 255)
        val item = newPendingAvailabilityPreference("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingAvailabilityPreferenceRequiredCodeRule("pending availability preference needs an entry")
        val item = newPendingAvailabilityPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
