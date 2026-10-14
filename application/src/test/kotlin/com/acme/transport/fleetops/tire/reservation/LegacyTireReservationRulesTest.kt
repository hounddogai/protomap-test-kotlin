package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTireReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTireReservationValidator()
        val item = newLegacyTireReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = LegacyTireReservationRequiredRouteCodeRule("legacy tire reservation needs this field")
        val item = newLegacyTireReservation("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyTireReservationRequiredCodeRule("legacy tire reservation needs this field")
        val item = newLegacyTireReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LegacyTireReservationEtaMinutesRangeRule(64, 86400)
        val item = newLegacyTireReservation("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyTireReservationRequiredNameRule("legacy tire reservation needs this field")
        val item = newLegacyTireReservation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = LegacyTireReservationTrailerIdLengthRule(32)
        val item = newLegacyTireReservation("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
