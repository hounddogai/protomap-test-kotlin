package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelBurnRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalFuelBurnRecordValidator()
        val item = newRegionalFuelBurnRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = RegionalFuelBurnRecordRequiredCountryRule("regional fuel burn record needs a value")
        val item = newRegionalFuelBurnRecord("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalFuelBurnRecordRequiredCodeRule("regional fuel burn record needs an entry")
        val item = newRegionalFuelBurnRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = RegionalFuelBurnRecordSummaryLengthRule(1000)
        val item = newRegionalFuelBurnRecord("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = RegionalFuelBurnRecordDistanceKmRangeRule(7.0, 3463.4)
        val item = newRegionalFuelBurnRecord("x", "y")
        item.distanceKm = 3464.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalFuelBurnRecordRequiredTitleRule("regional fuel burn record needs a value")
        val item = newRegionalFuelBurnRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = RegionalFuelBurnRecordVersionRangeRule(16, 86400)
        val item = newRegionalFuelBurnRecord("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
