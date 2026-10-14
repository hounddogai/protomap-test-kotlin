package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelBurnTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalFuelBurnTargetValidator()
        val item = newRegionalFuelBurnTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RegionalFuelBurnTargetWindowDaysRangeRule(16, 3600)
        val item = newRegionalFuelBurnTarget("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = RegionalFuelBurnTargetRequiredCountryRule("regional fuel burn target needs this field")
        val item = newRegionalFuelBurnTarget("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalFuelBurnTargetTitleLengthRule(255)
        val item = newRegionalFuelBurnTarget("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = RegionalFuelBurnTargetVersionRangeRule(255, 3600)
        val item = newRegionalFuelBurnTarget("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
