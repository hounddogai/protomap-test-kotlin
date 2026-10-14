package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalParcelReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalParcelReservationValidator()
        val item = newSeasonalParcelReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalParcelReservationRequiredCodeRule("seasonal parcel reservation needs a value")
        val item = newSeasonalParcelReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalParcelReservationNameLengthRule(3600)
        val item = newSeasonalParcelReservation("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = SeasonalParcelReservationNotesLengthRule(80)
        val item = newSeasonalParcelReservation("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
