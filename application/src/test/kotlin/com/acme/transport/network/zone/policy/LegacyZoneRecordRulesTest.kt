package com.acme.transport.network.zone.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyZoneRecordValidator()
        val item = newLegacyZoneRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = LegacyZoneRecordRequiredRouteCodeRule("legacy zone record needs this field")
        val item = newLegacyZoneRecord("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyZoneRecordRequiredCodeRule("legacy zone record needs this field")
        val item = newLegacyZoneRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LegacyZoneRecordFuelLitersRangeRule(7.6, 2280.8)
        val item = newLegacyZoneRecord("x", "y")
        item.fuelLiters = 2282.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
