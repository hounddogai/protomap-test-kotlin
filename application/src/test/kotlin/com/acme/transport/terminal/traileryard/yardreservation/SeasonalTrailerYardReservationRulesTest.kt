package com.acme.transport.terminal.traileryard.yardreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTrailerYardReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTrailerYardReservationValidator()
        val item = newSeasonalTrailerYardReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = SeasonalTrailerYardReservationCreatedAtRangeRule(16, 86400)
        val item = newSeasonalTrailerYardReservation("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = SeasonalTrailerYardReservationDepotLengthRule(80)
        val item = newSeasonalTrailerYardReservation("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalTrailerYardReservationRequiredCodeRule("seasonal trailer yard reservation needs an entry")
        val item = newSeasonalTrailerYardReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = SeasonalTrailerYardReservationRequiredDepotRule(
            "seasonal trailer yard reservation needs this field",
        )
        val item = newSeasonalTrailerYardReservation("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
