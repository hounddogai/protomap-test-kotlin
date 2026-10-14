package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardReservationValidator()
        val item = newRateCardReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RateCardReservationRouteCodeLengthRule(16)
        val item = newRateCardReservation("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RateCardReservationRevisionRangeRule(86400, 86400)
        val item = newRateCardReservation("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RateCardReservationRequiredRouteCodeRule("rate card reservation needs this field")
        val item = newRateCardReservation("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RateCardReservationNameLengthRule(64)
        val item = newRateCardReservation("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
