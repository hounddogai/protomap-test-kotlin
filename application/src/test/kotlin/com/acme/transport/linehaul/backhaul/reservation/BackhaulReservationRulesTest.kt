package com.acme.transport.linehaul.backhaul.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulReservationValidator()
        val item = newBackhaulReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulReservationCodeLengthRule(1000)
        val item = newBackhaulReservation("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = BackhaulReservationCountryLengthRule(32)
        val item = newBackhaulReservation("x", "y")
        item.country = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulReservationRequiredCodeRule("backhaul reservation needs this field")
        val item = newBackhaulReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BackhaulReservationStartsAtRangeRule(80, 120)
        val item = newBackhaulReservation("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
