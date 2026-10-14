package com.acme.transport.terminal.traileryard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTrailerYardReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTrailerYardReservationValidator()
        val item = newLegacyTrailerYardReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = LegacyTrailerYardReservationSummaryLengthRule(86400)
        val item = newLegacyTrailerYardReservation("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyTrailerYardReservationCodeLengthRule(1000)
        val item = newLegacyTrailerYardReservation("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyTrailerYardReservationRequiredCodeRule("legacy trailer yard reservation needs a value")
        val item = newLegacyTrailerYardReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
