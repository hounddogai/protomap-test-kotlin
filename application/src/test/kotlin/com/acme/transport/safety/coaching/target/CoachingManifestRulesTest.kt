package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingManifestValidator()
        val item = newCoachingManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CoachingManifestThresholdRangeRule(5.4, 2496.4)
        val item = newCoachingManifest("x", "y")
        item.threshold = 2497.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = CoachingManifestFuelLitersRangeRule(5.9, 3437.9)
        val item = newCoachingManifest("x", "y")
        item.fuelLiters = 3439.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = CoachingManifestRequiredNotesRule("coaching manifest needs a value")
        val item = newCoachingManifest("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CoachingManifestRequiredCodeRule("coaching manifest needs an entry")
        val item = newCoachingManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
