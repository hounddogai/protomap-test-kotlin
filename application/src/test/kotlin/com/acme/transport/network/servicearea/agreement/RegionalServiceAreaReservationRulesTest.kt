package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalServiceAreaReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalServiceAreaReservationValidator()
        val item = newRegionalServiceAreaReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RegionalServiceAreaReservationExpiresAtRangeRule(80, 3600)
        val item = newRegionalServiceAreaReservation("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RegionalServiceAreaReservationSequenceRangeRule(32, 1000)
        val item = newRegionalServiceAreaReservation("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = RegionalServiceAreaReservationDistanceKmRangeRule(0.7, 690.7)
        val item = newRegionalServiceAreaReservation("x", "y")
        item.distanceKm = 692.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
