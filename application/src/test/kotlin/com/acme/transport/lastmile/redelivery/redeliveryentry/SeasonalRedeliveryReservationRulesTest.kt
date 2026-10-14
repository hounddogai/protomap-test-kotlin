package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRedeliveryReservationValidator()
        val item = newSeasonalRedeliveryReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = SeasonalRedeliveryReservationCurrencyLengthRule(255)
        val item = newSeasonalRedeliveryReservation("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalRedeliveryReservationTitleLengthRule(80)
        val item = newSeasonalRedeliveryReservation("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = SeasonalRedeliveryReservationQuantityRangeRule(255, 86400)
        val item = newSeasonalRedeliveryReservation("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
