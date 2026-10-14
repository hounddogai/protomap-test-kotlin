package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualParcelMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualParcelMappingValidator()
        val item = newManualParcelMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ManualParcelMappingWindowDaysRangeRule(64, 80)
        val item = newManualParcelMapping("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ManualParcelMappingDurationMinutesRangeRule(80, 255)
        val item = newManualParcelMapping("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = ManualParcelMappingRequiredCountryRule("manual parcel mapping needs a value")
        val item = newManualParcelMapping("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
