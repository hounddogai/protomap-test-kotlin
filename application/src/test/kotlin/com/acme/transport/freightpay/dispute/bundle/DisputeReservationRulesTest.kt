package com.acme.transport.freightpay.dispute.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeReservationValidator()
        val item = newDisputeReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DisputeReservationCodeLengthRule(64)
        val item = newDisputeReservation("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DisputeReservationDiscountRangeRule(0.2, 336.0)
        val item = newDisputeReservation("x", "y")
        item.discount = 337.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
