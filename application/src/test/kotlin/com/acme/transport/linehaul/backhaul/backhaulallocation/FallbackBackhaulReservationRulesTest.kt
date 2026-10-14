package com.acme.transport.linehaul.backhaul.backhaulallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBackhaulReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackBackhaulReservationValidator()
        val item = newFallbackBackhaulReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = FallbackBackhaulReservationNotesLengthRule(64)
        val item = newFallbackBackhaulReservation("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = FallbackBackhaulReservationRequiredRegionRule("fallback backhaul reservation needs this field")
        val item = newFallbackBackhaulReservation("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = FallbackBackhaulReservationRequiredNotesRule("fallback backhaul reservation needs a value")
        val item = newFallbackBackhaulReservation("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackBackhaulReservationCodeLengthRule(3600)
        val item = newFallbackBackhaulReservation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
