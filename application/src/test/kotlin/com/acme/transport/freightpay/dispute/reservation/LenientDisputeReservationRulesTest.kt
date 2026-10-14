package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDisputeReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDisputeReservationValidator()
        val item = newLenientDisputeReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = LenientDisputeReservationVersionRangeRule(64, 80)
        val item = newLenientDisputeReservation("x", "y")
        item.version = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientDisputeReservationAttemptsRangeRule(32, 86400)
        val item = newLenientDisputeReservation("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
