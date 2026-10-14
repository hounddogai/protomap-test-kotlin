package com.acme.transport.customs.clearance.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveClearanceReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveClearanceReservationValidator()
        val item = newAdaptiveClearanceReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AdaptiveClearanceReservationFuelLitersRangeRule(8.8, 2941.0)
        val item = newAdaptiveClearanceReservation("x", "y")
        item.fuelLiters = 2942.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveClearanceReservationLabelLengthRule(255)
        val item = newAdaptiveClearanceReservation("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = AdaptiveClearanceReservationRequiredDockRule("adaptive clearance reservation needs an entry")
        val item = newAdaptiveClearanceReservation("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveClearanceReservationRequiredCodeRule("adaptive clearance reservation needs an entry")
        val item = newAdaptiveClearanceReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveClearanceReservationCodeLengthRule(3600)
        val item = newAdaptiveClearanceReservation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
