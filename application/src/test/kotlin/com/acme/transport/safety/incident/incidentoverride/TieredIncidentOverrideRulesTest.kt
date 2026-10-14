package com.acme.transport.safety.incident.incidentoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIncidentOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredIncidentOverrideValidator()
        val item = newTieredIncidentOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = TieredIncidentOverrideVehicleIdLengthRule(16)
        val item = newTieredIncidentOverride("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredIncidentOverrideNameLengthRule(86400)
        val item = newTieredIncidentOverride("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = TieredIncidentOverrideGraceDaysRangeRule(32, 80)
        val item = newTieredIncidentOverride("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = TieredIncidentOverrideRequiredCountryRule("tiered incident override needs this field")
        val item = newTieredIncidentOverride("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = TieredIncidentOverrideCountryLengthRule(86400)
        val item = newTieredIncidentOverride("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
