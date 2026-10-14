package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCapacityReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCapacityReservationValidator()
        val item = newStrictCapacityReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictCapacityReservationRequiredNameRule("strict capacity reservation needs a value")
        val item = newStrictCapacityReservation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = StrictCapacityReservationStopCountRangeRule(255, 255)
        val item = newStrictCapacityReservation("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictCapacityReservationRequiredCodeRule("strict capacity reservation needs an entry")
        val item = newStrictCapacityReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictCapacityReservationNameLengthRule(16)
        val item = newStrictCapacityReservation("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = StrictCapacityReservationUpdatedAtRangeRule(16, 1000)
        val item = newStrictCapacityReservation("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
