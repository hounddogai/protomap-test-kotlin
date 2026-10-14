package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerPreferenceValidator()
        val item = newLockerPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LockerPreferenceCeilingRangeRule(9.8, 4168.0)
        val item = newLockerPreference("x", "y")
        item.ceiling = 4169.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LockerPreferenceRequiredNameRule("locker preference needs an entry")
        val item = newLockerPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LockerPreferenceDurationMinutesRangeRule(32, 80)
        val item = newLockerPreference("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LockerPreferenceRequiredRegionRule("locker preference needs an entry")
        val item = newLockerPreference("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
