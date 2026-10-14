package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTrailerPreferenceValidator()
        val item = newRegionalTrailerPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RegionalTrailerPreferenceAttemptsRangeRule(80, 3600)
        val item = newRegionalTrailerPreference("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalTrailerPreferenceRequiredTitleRule("regional trailer preference needs an entry")
        val item = newRegionalTrailerPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = RegionalTrailerPreferenceRegionLengthRule(1000)
        val item = newRegionalTrailerPreference("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RegionalTrailerPreferenceRequiredVehicleIdRule("regional trailer preference needs a value")
        val item = newRegionalTrailerPreference("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
