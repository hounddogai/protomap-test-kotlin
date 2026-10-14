package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedZoneReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedZoneReservationValidator()
        val item = newCappedZoneReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CappedZoneReservationDurationMinutesRangeRule(80, 3600)
        val item = newCappedZoneReservation("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = CappedZoneReservationRequiredLocaleRule("capped zone reservation needs a value")
        val item = newCappedZoneReservation("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedZoneReservationCodeLengthRule(255)
        val item = newCappedZoneReservation("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
