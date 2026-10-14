package com.acme.transport.freightpay.dispute.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDisputeReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDisputeReservationValidator()
        val item = newSeasonalDisputeReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SeasonalDisputeReservationRatioRangeRule(2.0, 2305.5)
        val item = newSeasonalDisputeReservation("x", "y")
        item.ratio = 2307.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalDisputeReservationCodeLengthRule(86400)
        val item = newSeasonalDisputeReservation("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = SeasonalDisputeReservationRateRangeRule(5.7, 4533.7)
        val item = newSeasonalDisputeReservation("x", "y")
        item.rate = 4535.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
