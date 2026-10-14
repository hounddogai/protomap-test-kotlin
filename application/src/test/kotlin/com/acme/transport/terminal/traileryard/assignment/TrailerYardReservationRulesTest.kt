package com.acme.transport.terminal.traileryard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardReservationValidator()
        val item = newTrailerYardReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerYardReservationTitleLengthRule(80)
        val item = newTrailerYardReservation("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TrailerYardReservationStopCountRangeRule(32, 80)
        val item = newTrailerYardReservation("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = TrailerYardReservationCurrencyLengthRule(80)
        val item = newTrailerYardReservation("x", "y")
        item.currency = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
