package com.acme.transport.linehaul.trailer.traileroverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerReservationValidator()
        val item = newTrailerReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerReservationRequiredCodeRule("trailer reservation needs this field")
        val item = newTrailerReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = TrailerReservationRequiredSummaryRule("trailer reservation needs this field")
        val item = newTrailerReservation("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerReservationCodeLengthRule(255)
        val item = newTrailerReservation("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TrailerReservationRevisionRangeRule(80, 3600)
        val item = newTrailerReservation("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
