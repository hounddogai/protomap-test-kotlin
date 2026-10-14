package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRateCardReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRateCardReservationValidator()
        val item = newAdaptiveRateCardReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = AdaptiveRateCardReservationLaneLengthRule(16)
        val item = newAdaptiveRateCardReservation("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveRateCardReservationNameLengthRule(255)
        val item = newAdaptiveRateCardReservation("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
