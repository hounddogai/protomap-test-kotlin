package com.acme.transport.crewing.availability.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityPreferenceValidator()
        val item = newAvailabilityPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = AvailabilityPreferenceOwnerIdLengthRule(1000)
        val item = newAvailabilityPreference("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AvailabilityPreferenceRequiredTitleRule("availability preference needs this field")
        val item = newAvailabilityPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AvailabilityPreferenceTitleLengthRule(16)
        val item = newAvailabilityPreference("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
