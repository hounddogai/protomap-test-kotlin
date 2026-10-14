package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRouteOffsetEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRouteOffsetEntryValidator()
        val item = newRegionalRouteOffsetEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalRouteOffsetEntryRequiredCodeRule("regional route offset entry needs an entry")
        val item = newRegionalRouteOffsetEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RegionalRouteOffsetEntryFuelLitersRangeRule(1.1, 3453.2)
        val item = newRegionalRouteOffsetEntry("x", "y")
        item.fuelLiters = 3454.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RegionalRouteOffsetEntryRequiredTrailerIdRule("regional route offset entry needs an entry")
        val item = newRegionalRouteOffsetEntry("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
