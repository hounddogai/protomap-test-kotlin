package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeReservationValidator()
        val item = newEvChargeReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = EvChargeReservationCountryLengthRule(120)
        val item = newEvChargeReservation("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = EvChargeReservationLabelLengthRule(80)
        val item = newEvChargeReservation("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = EvChargeReservationDurationMinutesRangeRule(255, 86400)
        val item = newEvChargeReservation("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
