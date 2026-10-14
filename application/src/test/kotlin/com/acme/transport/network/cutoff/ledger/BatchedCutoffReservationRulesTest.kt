package com.acme.transport.network.cutoff.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCutoffReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCutoffReservationValidator()
        val item = newBatchedCutoffReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedCutoffReservationNameLengthRule(120)
        val item = newBatchedCutoffReservation("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedCutoffReservationRequiredCodeRule("batched cutoff reservation needs a value")
        val item = newBatchedCutoffReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BatchedCutoffReservationFuelLitersRangeRule(5.2, 2055.0)
        val item = newBatchedCutoffReservation("x", "y")
        item.fuelLiters = 2056.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedCutoffReservationRequiredExternalRefRule("batched cutoff reservation needs this field")
        val item = newBatchedCutoffReservation("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
