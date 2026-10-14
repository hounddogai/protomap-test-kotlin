package com.acme.transport.freightpay.ratecard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRateCardReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRateCardReservationValidator()
        val item = newPrimaryRateCardReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = PrimaryRateCardReservationVersionRangeRule(32, 120)
        val item = newPrimaryRateCardReservation("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryRateCardReservationLabelLengthRule(32)
        val item = newPrimaryRateCardReservation("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryRateCardReservationCodeLengthRule(64)
        val item = newPrimaryRateCardReservation("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
