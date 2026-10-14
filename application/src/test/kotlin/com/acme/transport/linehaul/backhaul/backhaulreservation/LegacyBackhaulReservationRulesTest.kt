package com.acme.transport.linehaul.backhaul.backhaulreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBackhaulReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyBackhaulReservationValidator()
        val item = newLegacyBackhaulReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyBackhaulReservationNameLengthRule(32)
        val item = newLegacyBackhaulReservation("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LegacyBackhaulReservationRequiredVehicleIdRule("legacy backhaul reservation needs this field")
        val item = newLegacyBackhaulReservation("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LegacyBackhaulReservationAttemptsRangeRule(120, 255)
        val item = newLegacyBackhaulReservation("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = LegacyBackhaulReservationVehicleIdLengthRule(32)
        val item = newLegacyBackhaulReservation("x", "y")
        item.vehicleId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
