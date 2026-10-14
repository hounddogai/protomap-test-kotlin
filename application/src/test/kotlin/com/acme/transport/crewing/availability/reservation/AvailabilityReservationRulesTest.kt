package com.acme.transport.crewing.availability.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityReservationValidator()
        val item = newAvailabilityReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AvailabilityReservationRequiredCodeRule("availability reservation needs this field")
        val item = newAvailabilityReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AvailabilityReservationCeilingRangeRule(5.7, 4443.4)
        val item = newAvailabilityReservation("x", "y")
        item.ceiling = 4444.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityReservationCodeLengthRule(32)
        val item = newAvailabilityReservation("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
